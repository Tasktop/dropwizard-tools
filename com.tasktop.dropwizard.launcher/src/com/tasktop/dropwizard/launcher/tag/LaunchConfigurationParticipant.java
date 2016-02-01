/*******************************************************************************
 * Copyright (c) 2016 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Tasktop EULA
 * which accompanies this distribution, and is available at
 * http://tasktop.com/legal
 *******************************************************************************/

package com.tasktop.dropwizard.launcher.tag;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.google.common.collect.ImmutableMap;
import com.tasktop.dropwizard.launcher.DropwizardLaunchExceptionHandler;

public abstract class LaunchConfigurationParticipant {

	private final LaunchDialogUpdater updater;

	private final Map<String, String> attributes;

	public LaunchConfigurationParticipant(LaunchDialogUpdater updater) {
		this.updater = updater;
		this.attributes = new HashMap<>();
	}

	public abstract void initializeFrom(ILaunchConfiguration configuration) throws CoreException;

	public abstract IStatus validate() throws CoreException;

	public void setAttribute(String key, String value) {
		IStatus status;
		try {
			status = validate();
			if (status.isOK()) {
				attributes.put(key, value);
			} else {
				updater.updateError(status.getMessage());
			}
			updater.update();
		} catch (CoreException e) {
			DropwizardLaunchExceptionHandler.handle(e);
		}
	}

	public Map<String, String> getAttributes() {
		return ImmutableMap.copyOf(attributes);
	}

}
