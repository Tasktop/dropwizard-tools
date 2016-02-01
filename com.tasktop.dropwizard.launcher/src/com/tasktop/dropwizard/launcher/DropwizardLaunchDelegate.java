/*******************************************************************************
 * Copyright (c) 2016 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Tasktop EULA
 * which accompanies this distribution, and is available at
 * http://tasktop.com/legal
 *******************************************************************************/

package com.tasktop.dropwizard.launcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

public class DropwizardLaunchDelegate extends JavaLaunchDelegate {

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		RunningInstanceEliminator instanceEliminator = new RunningInstanceEliminator(configuration);
		instanceEliminator.eliminateRunningInstances();
		return super.preLaunchCheck(configuration, mode, monitor);
	}

	@Override
	public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		StringBuilder argumentBuilder = new StringBuilder();
		String mode = configuration.getAttribute(DropwizardLaunchConstants.ATTR_MODE, "server");
		argumentBuilder.append(mode);
		String configFile = configuration.getAttribute(DropwizardLaunchConstants.ATTR_CONFIG_FILE, "");
		if (!configFile.isEmpty()) {
			appendConfigFile(configuration, argumentBuilder, configFile);
		}
		argumentBuilder.append(" " + super.getProgramArguments(configuration));
		return argumentBuilder.toString();
	}

	private void appendConfigFile(ILaunchConfiguration configuration, StringBuilder argumentBuilder, String configFile)
			throws CoreException {
		String configFileProject = configuration.getAttribute(DropwizardLaunchConstants.ATTR_CONFIG_FILE_PROJECT,
				"");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(configFileProject);
		IFile file = (IFile) project.findMember(configFile);
		argumentBuilder.append(" \"");
		argumentBuilder.append(file.getLocation().toString());
		argumentBuilder.append("\"");
	}

}
