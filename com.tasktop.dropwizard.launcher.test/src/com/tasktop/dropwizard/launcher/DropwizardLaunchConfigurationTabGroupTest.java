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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.junit.Test;

import com.tasktop.dropwizard.launcher.tabs.DropwizardApplicationTab;

public class DropwizardLaunchConfigurationTabGroupTest {

	@Test
	public void testHasTabs() {
		DropwizardLaunchConfigurationTabGroup group = new DropwizardLaunchConfigurationTabGroup();
		group.createTabs(mock(ILaunchConfigurationDialog.class), "debug");

		ILaunchConfigurationTab[] tabs = group.getTabs();

		assertEquals(7, tabs.length);
		assertTrue(tabs[0] instanceof DropwizardApplicationTab);
		assertTrue(tabs[1] instanceof JavaArgumentsTab);
		assertTrue(tabs[2] instanceof JavaJRETab);
		assertTrue(tabs[3] instanceof JavaClasspathTab);
		assertTrue(tabs[4] instanceof SourceLookupTab);
		assertTrue(tabs[5] instanceof EnvironmentTab);
		assertTrue(tabs[6] instanceof CommonTab);
	}

}
