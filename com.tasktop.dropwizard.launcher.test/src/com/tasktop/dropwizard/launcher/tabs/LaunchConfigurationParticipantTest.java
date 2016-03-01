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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.Test;

import com.tasktop.dropwizard.launcher.DropwizardLaunchConstants;

public class LaunchConfigurationParticipantTest {

	@Test
	public void testStoresAttributes() {
		LaunchDialogUpdater updater = mock(LaunchDialogUpdater.class);
		IStatus status = new Status(IStatus.OK, DropwizardLaunchConstants.PLUGIN_ID, "foo");
		TestLaunchConfigParticipant participant = spy(new TestLaunchConfigParticipant(updater, status));

		participant.setAttribute("foo", "bar");
		Map<String, String> attributes = participant.getAttributes();

		assertEquals("bar", attributes.get("foo"));
	}

	@Test
	public void testSetAttributeInvokesUpdateWithStatusOK() {
		LaunchDialogUpdater updater = mock(LaunchDialogUpdater.class);
		IStatus status = new Status(IStatus.OK, DropwizardLaunchConstants.PLUGIN_ID, "foo");
		TestLaunchConfigParticipant participant = spy(new TestLaunchConfigParticipant(updater, status));

		participant.setAttribute("foo", "bar");

		verify(updater).updateError(null);
		verify(updater).update();
	}

	@Test
	public void testSetAttributeInvokesUpdateWithStatusERROR() {
		LaunchDialogUpdater updater = mock(LaunchDialogUpdater.class);
		IStatus status = new Status(IStatus.ERROR, DropwizardLaunchConstants.PLUGIN_ID, "foo");
		TestLaunchConfigParticipant participant = spy(new TestLaunchConfigParticipant(updater, status));

		participant.setAttribute("foo", "bar");

		verify(updater).updateError("foo");
		verify(updater).update();
	}

	private static class TestLaunchConfigParticipant extends LaunchConfigurationParticipant {

		private final IStatus validationStatus;

		public TestLaunchConfigParticipant(LaunchDialogUpdater updater, IStatus validationStatus) {
			super(updater);
			this.validationStatus = validationStatus;
		}

		@Override
		public void initializeFrom(ILaunchConfiguration configuration) throws CoreException {
		}

		@Override
		public IStatus validate() throws CoreException {
			return validationStatus;
		}

	}

}
