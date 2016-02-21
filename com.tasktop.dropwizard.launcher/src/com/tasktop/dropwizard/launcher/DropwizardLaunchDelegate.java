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
package com.tasktop.dropwizard.launcher;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
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
		String configFileProject = configuration.getAttribute(DropwizardLaunchConstants.ATTR_CONFIG_FILE_PROJECT, "");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(configFileProject);
		if (project != null && project.isAccessible()) {
			appendConfigFile(argumentBuilder, configFile, project);
		} else {
			abort("Project " + configFileProject + " is closed", new CoreException(Status.CANCEL_STATUS),
					IJavaLaunchConfigurationConstants.ERR_PROJECT_CLOSED);
		}
	}

	private void appendConfigFile(StringBuilder argumentBuilder, String configFile, IProject project)
			throws CoreException {
		IFile file = (IFile) project.findMember(configFile);
		if (file != null) {
			argumentBuilder.append(" \"");
			argumentBuilder.append(file.getLocation().toString());
			argumentBuilder.append("\"");
		} else {
			abort("Config file does not exist: " + project.getName() + File.separator + configFile,
					new CoreException(Status.CANCEL_STATUS), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
	}

}
