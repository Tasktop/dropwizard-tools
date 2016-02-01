/**
 * (C) Copyright (c) 2016 Tasktop Technologies and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Holger Staudacher - initial implementation
 */
package com.tasktop.dropwizard.launcher.tag;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.tasktop.dropwizard.launcher.DropwizardLaunchConstants;
import com.tasktop.dropwizard.launcher.DropwizardLaunchExceptionHandler;

public class ApplicationPart extends LaunchConfigurationParticipant {

	private IType applicationType;

	private Text text;

	private String projectName;

	public ApplicationPart(LaunchDialogUpdater updater) {
		super(updater);
		try {
			applicationType = findApplicationType();
		} catch (CoreException e) {
			DropwizardLaunchExceptionHandler.handle(e, Display.getCurrent().getActiveShell(), "Application not found",
					"No project found in workspace having a Dropwizard dependency.");
		}
	}

	private IType findApplicationType() throws JavaModelException, CoreException {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject iProject : projects) {
			if (iProject.isOpen() && iProject.hasNature(JavaCore.NATURE_ID)) {
				IType type = JavaCore.create(iProject).findType("io.dropwizard.Application");
				if (type != null) {
					return type;
				}
			}
		}
		return null;
	}

	public void createControl(final Composite parent) {
		text = new Text(parent, SWT.SINGLE | SWT.LEAD | SWT.BORDER | SWT.READ_ONLY);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		text.setText("");
		Button browseButton = new Button(parent, SWT.PUSH);
		browseButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				openTypeDialog(parent);
			}

		});
	}

	private void openTypeDialog(Composite parent) {
		Shell shell = parent.getShell();
		try {
			SelectionDialog typeDialog = JavaUI.createTypeDialog(shell, new ProgressMonitorDialog(shell),
					SearchEngine.createHierarchyScope(applicationType), IJavaElementSearchConstants.CONSIDER_CLASSES,
					false, "*Application");
			typeDialog.setTitle("Select Application");
			if (typeDialog.open() == Window.OK) {
				Object[] result = typeDialog.getResult();
				if (result.length > 0) {
					IType jdtType = (IType) result[0];
					text.setText(jdtType.getFullyQualifiedName());
					projectName = jdtType.getJavaProject().getElementName();
					setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
							jdtType.getFullyQualifiedName());
					setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
				}
			}
		} catch (JavaModelException e) {
			DropwizardLaunchExceptionHandler.handle(e);
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) throws CoreException {
		String type = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "");
		text.setText(type);
		projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
	}

	@Override
	public IStatus validate() throws CoreException {
		String typeName = text.getText();
		if (!projectName.isEmpty()) {
			return computeStatusBasedOnType(typeName);
		}
		return new Status(IStatus.ERROR, DropwizardLaunchConstants.PLUGIN_ID, "No Application selected.");
	}

	private IStatus computeStatusBasedOnType(String typeName) throws JavaModelException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project.isOpen()) {
			IJavaProject javaProject = JavaCore.create(project);
			IType type = javaProject.findType(typeName);
			if (type == null) {
				return new Status(IStatus.ERROR, DropwizardLaunchConstants.PLUGIN_ID,
						"Application Type not found in project " + projectName);
			}
		} else {
			return new Status(IStatus.ERROR, DropwizardLaunchConstants.PLUGIN_ID,
					"Application Type not found in project " + projectName);
		}
		return Status.OK_STATUS;
	}

}
