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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaApplicationLaunchShortcut;

public class DropwizardLaunchShortcut extends JavaApplicationLaunchShortcut {

	private final ILaunchManager launchManager;

	public DropwizardLaunchShortcut() {
		this(DebugPlugin.getDefault().getLaunchManager());
	}

	public DropwizardLaunchShortcut(ILaunchManager launchManager) {
		this.launchManager = launchManager;
	}

	@Override
	protected ILaunchConfigurationType getConfigurationType() {
		return launchManager.getLaunchConfigurationType(DropwizardLaunchConstants.ID_DROPWIZARD_APPLICATION);
	}

	@Override
	protected ILaunchConfiguration createConfiguration(IType type) {
		ILaunchConfiguration configuration = super.createConfiguration(type);
		try {
			ILaunchConfigurationWorkingCopy copy = configuration
					.copy(launchManager.generateLaunchConfigurationName(type.getTypeQualifiedName('.')));
			configuration.delete();
			copy = copy.copy(launchManager.generateLaunchConfigurationName(type.getTypeQualifiedName('.')));
			copy.setAttribute(DropwizardLaunchConstants.ATTR_CONFIG_FILE, getConfigFile(type));
			copy.setAttribute(DropwizardLaunchConstants.ATTR_CONFIG_FILE_PROJECT,
					type.getJavaProject().getElementName());
			copy.setAttribute(DropwizardLaunchConstants.ATTR_MODE, "server");
			return copy.doSave();
		} catch (CoreException e) {
			DropwizardLaunchExceptionHandler.handle(e);
		}
		return configuration;
	}

	private String getConfigFile(IType type) {
		IProject project = type.getJavaProject().getProject();
		try {
			IResource[] members = project.members();
			return lookForYmlFile(members);
		} catch (CoreException e) {
			DropwizardLaunchExceptionHandler.handle(e);
		}
		return "";
	}

	private String lookForYmlFile(IResource[] members) throws CoreException {
		for (IResource resource : members) {
			if ("yml".equals(resource.getFileExtension()) || "yaml".equals(resource.getFileExtension())) {
				return resource.getProjectRelativePath().toString();
			}
			if (resource instanceof IFolder) {
				IFolder folder = (IFolder) resource;
				IResource[] children = folder.members();
				String result = lookForYmlFile(children);
				if (!result.isEmpty()) {
					return result;
				}
			}
		}
		return "";
	}

}
