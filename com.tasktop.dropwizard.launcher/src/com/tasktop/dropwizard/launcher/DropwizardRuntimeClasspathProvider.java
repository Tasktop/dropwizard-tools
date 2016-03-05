/*******************************************************************************
 * Copyright (c) 2008-2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation
 *******************************************************************************/

package com.tasktop.dropwizard.launcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardClasspathProvider;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ICallable;
import org.eclipse.m2e.core.embedder.IMavenExecutionContext;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.jdt.IClassifierClasspathProvider;
import org.eclipse.m2e.jdt.IClasspathManager;
import org.eclipse.m2e.jdt.IMavenClassifierManager;
import org.eclipse.m2e.jdt.MavenJdtPlugin;
import org.eclipse.m2e.jdt.internal.MavenClasspathHelpers;

@SuppressWarnings("restriction")
public class DropwizardRuntimeClasspathProvider extends StandardClasspathProvider {

	private static final String THIS_PROJECT_CLASSIFIER = ""; //$NON-NLS-1$

	IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();

	public static void enable(ILaunchConfiguration config) {
		try {
			if (config instanceof ILaunchConfigurationWorkingCopy) {
				enable((ILaunchConfigurationWorkingCopy) config);
			} else {
				ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
				enable(wc);
				wc.doSave();
			}
		} catch (CoreException e) {
			DropwizardLaunchExceptionHandler.handle(e);
		}
	}

	private static void enable(ILaunchConfigurationWorkingCopy wc) {
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER,
				DropwizardLaunchConstants.DROPWIZARD_CLASSPATH_PROVIDER);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER,
				DropwizardLaunchConstants.DROPWIZARD_SOURCEPATH_PROVIDER);
	}

	@Override
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(final ILaunchConfiguration configuration)
			throws CoreException {
		boolean useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
		if (useDefault) {
			IJavaProject javaProject = JavaRuntime.getJavaProject(configuration);
			IRuntimeClasspathEntry jreEntry = JavaRuntime.computeJREEntry(configuration);
			IRuntimeClasspathEntry projectEntry = JavaRuntime.newProjectRuntimeClasspathEntry(javaProject);
			IRuntimeClasspathEntry mavenEntry = JavaRuntime.newRuntimeContainerClasspathEntry(
					new Path(IClasspathManager.CONTAINER_ID), IRuntimeClasspathEntry.USER_CLASSES);

			if (jreEntry == null) {
				return new IRuntimeClasspathEntry[] { projectEntry, mavenEntry };
			}

			return new IRuntimeClasspathEntry[] { jreEntry, projectEntry, mavenEntry };
		}

		return recoverRuntimePath(configuration, IJavaLaunchConfigurationConstants.ATTR_CLASSPATH);
	}

	@Override
	public IRuntimeClasspathEntry[] resolveClasspath(final IRuntimeClasspathEntry[] entries,
			final ILaunchConfiguration configuration) throws CoreException {
		IProgressMonitor monitor = new NullProgressMonitor();
		return MavenPlugin.getMaven().execute(new ICallable<IRuntimeClasspathEntry[]>() {

			@Override
			public IRuntimeClasspathEntry[] call(IMavenExecutionContext context, IProgressMonitor monitor)
					throws CoreException {
				return resolveClasspath0(entries, configuration, monitor);
			}
		}, monitor);
	}

	IRuntimeClasspathEntry[] resolveClasspath0(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration,
			IProgressMonitor monitor) throws CoreException {
		int scope = getArtifactScope(configuration);
		Set<IRuntimeClasspathEntry> all = new LinkedHashSet<IRuntimeClasspathEntry>(entries.length);
		for (IRuntimeClasspathEntry entry : entries) {
			if (entry.getType() == IRuntimeClasspathEntry.CONTAINER
					&& MavenClasspathHelpers.isMaven2ClasspathContainer(entry.getPath())) {
				addMavenClasspathEntries(all, entry, configuration, scope, monitor);
			} else if (entry.getType() == IRuntimeClasspathEntry.PROJECT) {
				IJavaProject javaProject = JavaRuntime.getJavaProject(configuration);
				if (javaProject.getPath().equals(entry.getPath())) {
					addProjectEntries(all, entry.getPath(), scope, THIS_PROJECT_CLASSIFIER, configuration, monitor);
				} else {
					addStandardClasspathEntries(all, entry, configuration);
				}
			} else {
				addStandardClasspathEntries(all, entry, configuration);
			}
		}
		return all.toArray(new IRuntimeClasspathEntry[all.size()]);
	}

	private void addStandardClasspathEntries(Set<IRuntimeClasspathEntry> all, IRuntimeClasspathEntry entry,
			ILaunchConfiguration configuration) throws CoreException {
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(entry, configuration);
		for (int j = 0; j < resolved.length; j++) {
			all.add(resolved[j]);
		}
	}

	private void addMavenClasspathEntries(Set<IRuntimeClasspathEntry> resolved,
			IRuntimeClasspathEntry runtimeClasspathEntry, ILaunchConfiguration configuration, int scope,
			IProgressMonitor monitor) throws CoreException {
		IJavaProject javaProject = JavaRuntime.getJavaProject(configuration);
		MavenJdtPlugin plugin = MavenJdtPlugin.getDefault();
		IClasspathManager buildpathManager = plugin.getBuildpathManager();
		IClasspathEntry[] cp = buildpathManager.getClasspath(javaProject.getProject(), scope, false, monitor);
		for (IClasspathEntry entry : cp) {
			switch (entry.getEntryKind()) {
			case IClasspathEntry.CPE_PROJECT:
				addProjectEntries(resolved, entry.getPath(), scope, getArtifactClassifier(entry), configuration,
						monitor);
				break;
			case IClasspathEntry.CPE_LIBRARY:
				resolved.add(JavaRuntime.newArchiveRuntimeClasspathEntry(entry.getPath()));
				break;
			default:
				break;
			}
		}
	}

	protected int getArtifactScope(ILaunchConfiguration configuration) throws CoreException {
		String typeid = configuration.getType().getAttribute("id"); //$NON-NLS-1$
		if (DropwizardLaunchConstants.ID_DROPWIZARD_APPLICATION.equals(typeid)) {
			IResource[] resources = configuration.getMappedResources();

			// MNGECLIPSE-530: NPE starting openarchitecture workflow 
			if (resources == null || resources.length == 0) {
				return IClasspathManager.CLASSPATH_RUNTIME;
			}

			// ECLIPSE-33: applications from test sources should use test scope 
			final Set<IPath> testSources = new HashSet<IPath>();
			IJavaProject javaProject = JavaRuntime.getJavaProject(configuration);
			IMavenProjectFacade facade = projectManager.create(javaProject.getProject(), new NullProgressMonitor());
			if (facade == null) {
				return IClasspathManager.CLASSPATH_RUNTIME;
			}

			testSources.addAll(Arrays.asList(facade.getTestCompileSourceLocations()));

			for (int i = 0; i < resources.length; i++) {
				for (IPath testPath : testSources) {
					if (testPath.isPrefixOf(resources[i].getProjectRelativePath())) {
						return IClasspathManager.CLASSPATH_TEST;
					}
				}
			}
			return IClasspathManager.CLASSPATH_RUNTIME;
		}
		throw new CoreException(
				new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, 0, "Unsupported launch configuration type", null));
	}

	protected void addProjectEntries(Set<IRuntimeClasspathEntry> resolved, IPath path, int scope, String classifier,
			ILaunchConfiguration launchConfiguration, final IProgressMonitor monitor) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(path.segment(0));

		IMavenProjectFacade projectFacade = projectManager.create(project, monitor);
		if (projectFacade == null) {
			return;
		}

		ResolverConfiguration configuration = projectFacade.getResolverConfiguration();
		if (configuration == null) {
			return;
		}

		IJavaProject javaProject = JavaCore.create(project);

		boolean projectResolved = false;

		for (IClasspathEntry entry : javaProject.getRawClasspath()) {
			IRuntimeClasspathEntry rce = null;
			switch (entry.getEntryKind()) {
			case IClasspathEntry.CPE_SOURCE:
				if (!projectResolved) {

					IMavenClassifierManager mavenClassifierManager = MavenJdtPlugin.getDefault()
							.getMavenClassifierManager();
					IClassifierClasspathProvider classifierClasspathProvider = mavenClassifierManager
							.getClassifierClasspathProvider(projectFacade, classifier);

					if (IClasspathManager.CLASSPATH_TEST == scope) {
						classifierClasspathProvider.setTestClasspath(resolved, projectFacade, monitor);
					} else {
						classifierClasspathProvider.setRuntimeClasspath(resolved, projectFacade, monitor);
					}

					projectResolved = true;
				}
				break;
			case IClasspathEntry.CPE_CONTAINER:
				IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), javaProject);
				if (container != null && !MavenClasspathHelpers.isMaven2ClasspathContainer(entry.getPath())) {
					switch (container.getKind()) {
					case IClasspathContainer.K_APPLICATION:
						rce = JavaRuntime.newRuntimeContainerClasspathEntry(container.getPath(),
								IRuntimeClasspathEntry.USER_CLASSES, javaProject);
						break;
					default:
						break;
					}
				}
				break;
			case IClasspathEntry.CPE_LIBRARY:
				rce = JavaRuntime.newArchiveRuntimeClasspathEntry(entry.getPath());
				break;
			case IClasspathEntry.CPE_VARIABLE:
				if (!JavaRuntime.JRELIB_VARIABLE.equals(entry.getPath().segment(0))) {
					rce = JavaRuntime.newVariableRuntimeClasspathEntry(entry.getPath());
				}
				break;
			case IClasspathEntry.CPE_PROJECT:
				IProject res = root.getProject(entry.getPath().segment(0));
				if (res != null) {
					IJavaProject otherProject = JavaCore.create(res);
					if (otherProject != null) {
						rce = JavaRuntime.newDefaultProjectClasspathEntry(otherProject);
					}
				}
				break;
			default:
				break;
			}
			if (rce != null) {
				addStandardClasspathEntries(resolved, rce, launchConfiguration);
			}
		}
	}

	private String getArtifactClassifier(IClasspathEntry entry) {
		IClasspathAttribute[] attributes = entry.getExtraAttributes();
		for (IClasspathAttribute attribute : attributes) {
			if (IClasspathManager.CLASSIFIER_ATTRIBUTE.equals(attribute.getName())) {
				return attribute.getValue();
			}
		}
		return null;
	}

}
