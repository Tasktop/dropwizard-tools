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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.tasktop.dropwizard.launcher.DropwizardLaunchExceptionHandler;

public class DropwizardApplicationTab extends JavaMainTab implements LaunchDialogUpdater {

	List<LaunchConfigurationParticipant> participants = new ArrayList<>();

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite composite = (Composite) getControl();
		createConfigurationGroup(composite);
	}

	private void createConfigurationGroup(Composite composite) {
		Group applicationGroup = new Group(composite, SWT.NONE);
		applicationGroup.setLayoutData(
				GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).span(2, 1).create());
		applicationGroup.setLayout(new GridLayout(2, false));
		applicationGroup.setText("Configuration:");
		ConfigurationPart configurationPart = new ConfigurationPart(this);
		configurationPart.createControl(applicationGroup);
		participants.add(configurationPart);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		try {
			for (LaunchConfigurationParticipant participant : participants) {
				participant.initializeFrom(configuration);
			}
		} catch (CoreException e) {
			DropwizardLaunchExceptionHandler.handle(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		for (LaunchConfigurationParticipant participant : participants) {
			Map<String, String> attributes = participant.getAttributes();
			Set<Entry<String, String>> entrySet = attributes.entrySet();
			for (Entry<String, String> entry : entrySet) {
				configuration.setAttribute(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public String getName() {
		return "Application";
	}

	@Override
	public Image getImage() {
		URL image = DropwizardApplicationTab.class.getResource("/dropwizard.png");
		return ImageDescriptor.createFromURL(image).createImage();
	}

	@Override
	public void update() {
		updateLaunchConfigurationDialog();
		scheduleUpdateJob();
	}

	@Override
	public void updateError(String message) {
		setErrorMessage(message);
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		boolean isValid = true;
		try {
			for (LaunchConfigurationParticipant participant : participants) {
				isValid &= participant.validate().isOK();
			}
		} catch (CoreException e) {
			DropwizardLaunchExceptionHandler.handle(e);
		}
		return super.isValid(launchConfig) && isValid;
	}

}
