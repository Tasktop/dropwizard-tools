/*******************************************************************************
 * Copyright (c) 2016 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Tasktop EULA
 * which accompanies this distribution, and is available at
 * http://tasktop.com/legal
 *******************************************************************************/

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

	@Override
	protected ILaunchConfigurationType getConfigurationType() {
		ILaunchManager launchManager = getLaunchManager();
		return launchManager.getLaunchConfigurationType(DropwizardLaunchConstants.ID_DROPWIZARD_APPLICATION);
	}

	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	@Override
	protected ILaunchConfiguration createConfiguration(IType type) {
		ILaunchConfiguration configuration = super.createConfiguration(type);
		try {
			ILaunchConfigurationWorkingCopy copy = configuration
					.copy(getLaunchManager().generateLaunchConfigurationName(type.getTypeQualifiedName('.')));
			configuration.delete();
			copy = copy.copy(getLaunchManager().generateLaunchConfigurationName(type.getTypeQualifiedName('.')));
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
