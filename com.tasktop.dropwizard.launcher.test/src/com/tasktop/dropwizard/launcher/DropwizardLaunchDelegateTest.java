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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DropwizardLaunchDelegateTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setUp() {
		new VariablesPlugin();
	}

	@Test
	public void testHasProgrammArguments() throws CoreException {
		IWorkspace workspace = createWorkspace();
		DropwizardLaunchDelegate delegate = new DropwizardLaunchDelegate(workspace);
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		when(configuration.getAttribute(DropwizardLaunchConstants.ATTR_CONFIG_FILE_PROJECT, "")).thenReturn("project");
		when(configuration.getAttribute(DropwizardLaunchConstants.ATTR_MODE, "server")).thenReturn("foo");
		when(configuration.getAttribute(DropwizardLaunchConstants.ATTR_CONFIG_FILE, "")).thenReturn("foo.yml");
		when(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "")).thenReturn("");

		String programArguments = delegate.getProgramArguments(configuration);

		assertEquals("foo \"path/foo.yml\" ", programArguments);
	}

	@Test
	public void testHasProgrammArgumentsAppendsJavaArgs() throws CoreException {
		IWorkspace workspace = createWorkspace();
		DropwizardLaunchDelegate delegate = new DropwizardLaunchDelegate(workspace);
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		when(configuration.getAttribute(DropwizardLaunchConstants.ATTR_CONFIG_FILE_PROJECT, "")).thenReturn("project");
		when(configuration.getAttribute(DropwizardLaunchConstants.ATTR_MODE, "server")).thenReturn("foo");
		when(configuration.getAttribute(DropwizardLaunchConstants.ATTR_CONFIG_FILE, "")).thenReturn("foo.yml");
		when(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""))
				.thenReturn("bar");

		String programArguments = delegate.getProgramArguments(configuration);

		assertEquals("foo \"path/foo.yml\" bar", programArguments);
	}

	private IWorkspace createWorkspace() {
		IWorkspace workspace = mock(IWorkspace.class);
		IWorkspaceRoot workspaceRoot = mock(IWorkspaceRoot.class);
		when(workspace.getRoot()).thenReturn(workspaceRoot);
		IProject project = mock(IProject.class);
		IResource config = mock(IFile.class);
		IPath path = mock(IPath.class);
		when(path.toString()).thenReturn("path/foo.yml");
		when(config.getLocation()).thenReturn(path);
		when(project.findMember(anyString())).thenReturn(config);
		when(project.isAccessible()).thenReturn(true);
		when(workspaceRoot.getProject("project")).thenReturn(project);
		return workspace;
	}

}
