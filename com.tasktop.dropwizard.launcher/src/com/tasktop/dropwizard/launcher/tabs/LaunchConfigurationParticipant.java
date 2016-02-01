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
package com.tasktop.dropwizard.launcher.tabs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;

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
				updater.updateError(null);
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
		return new HashMap<>(attributes);
	}

}
