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

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;

import com.tasktop.dropwizard.launcher.DropwizardLaunchConstants;
import com.tasktop.dropwizard.launcher.DropwizardPlugin;

public class ConfigurationPart extends LaunchConfigurationParticipant {

	private Text text;

	private Button serverButton;

	private Button healthcheckButton;

	public ConfigurationPart(LaunchDialogUpdater updater) {
		super(updater);
	}

	public void createControl(final Composite parent) {
		text = new Text(parent, SWT.SINGLE | SWT.LEAD | SWT.BORDER | SWT.READ_ONLY);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		text.setMessage("Dropwizard Configuration File");
		text.setText("");
		createBrowserButton(parent);
		createModeButtons(parent);
	}

	private void createBrowserButton(final Composite parent) {
		Button browseButton = new Button(parent, SWT.PUSH);
		browseButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				openResourceDialog(parent);
			}

		});
	}

	private void openResourceDialog(Composite parent) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		ResourceListSelectionDialog dialog = new ResourceListSelectionDialog(parent.getShell(), root, IResource.FILE);
		dialog.setTitle("Select Dropwizard Configuration");
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if (result.length > 0) {
				IFile file = (IFile) result[0];
				String path = file.getFullPath().toString();
				text.setText(path);
				setAttribute(DropwizardLaunchConstants.ATTR_CONFIG_FILE, file.getProjectRelativePath().toString());
				setAttribute(DropwizardLaunchConstants.ATTR_CONFIG_FILE_PROJECT, file.getProject().getName());
			}
		}
	}

	private void createModeButtons(Composite parent) {
		serverButton = new Button(parent, SWT.RADIO);
		GridDataFactory.swtDefaults()
				.align(SWT.BEGINNING, SWT.CENTER)
				.grab(true, false)
				.span(2, 1)
				.applyTo(serverButton);
		serverButton.setText("Server");
		healthcheckButton = new Button(parent, SWT.RADIO);
		healthcheckButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		healthcheckButton.setText("Healthcheck");
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) throws CoreException {
		if (configuration.hasAttribute(DropwizardLaunchConstants.ATTR_CONFIG_FILE)) {
			String configFile = configuration.getAttribute(DropwizardLaunchConstants.ATTR_CONFIG_FILE, "");
			String configFileProject = configuration.getAttribute(DropwizardLaunchConstants.ATTR_CONFIG_FILE_PROJECT,
					"");
			text.setText(configFileProject + File.separator + configFile);
		}
		String mode = configuration.getAttribute(DropwizardLaunchConstants.ATTR_MODE, "server");
		if (mode.equals("server")) {
			serverButton.setSelection(true);
		} else {
			healthcheckButton.setSelection(true);
		}
		hookSelectionListeners();
	}

	private void hookSelectionListeners() {
		serverButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (serverButton.getSelection()) {
					setAttribute(DropwizardLaunchConstants.ATTR_MODE, "server");
				}
			}
		});
		healthcheckButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (healthcheckButton.getSelection()) {
					setAttribute(DropwizardLaunchConstants.ATTR_MODE, "check");
				}
			}
		});
	}

	@Override
	public IStatus validate() throws CoreException {
		String configurationFile = text.getText();
		if (!configurationFile.isEmpty()) {
			boolean isValidConfig = configurationFile.endsWith(".yml") || configurationFile.endsWith(".yaml")
					|| configurationFile.endsWith(".json");
			if (!isValidConfig) {
				return new Status(IStatus.ERROR, DropwizardPlugin.PLUGIN_ID,
						"Configuration file must be a yaml or json file");
			}
		}
		return Status.OK_STATUS;
	}

}
