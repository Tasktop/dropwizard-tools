/*******************************************************************************
 * Copyright (c) 2016 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Tasktop EULA
 * which accompanies this distribution, and is available at
 * http://tasktop.com/legal
 *******************************************************************************/

package com.tasktop.dropwizard.launcher.tag;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.tasktop.dropwizard.launcher.DropwizardLaunchExceptionHandler;

public class DropwizardApplicationTab extends AbstractLaunchConfigurationTab implements LaunchDialogUpdater {

	List<LaunchConfigurationParticipant> participants = new ArrayList<>();

	@Override
	public void createControl(Composite parent) {
		Composite composite = createTabContent(parent);
		setControl(composite);
	}

	private Composite createTabContent(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, true));

		createApplicationGroup(composite);
		createConfigurationGroup(composite);

		return composite;
	}

	private void createApplicationGroup(Composite composite) {
		Group applicationGroup = new Group(composite, SWT.NONE);
		applicationGroup.setLayoutData(
				GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).span(2, 1).create());
		applicationGroup.setLayout(new GridLayout(2, false));
		applicationGroup.setText("Application:");
		ApplicationPart applicationPart = new ApplicationPart(this);
		applicationPart.createControl(applicationGroup);
		participants.add(applicationPart);
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
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
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
