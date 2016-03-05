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

public class DropwizardLaunchConstants {

	public static final String PLUGIN_ID = "com.tasktop.dropwizard.launcher";

	public static final String ID_DROPWIZARD_APPLICATION = "com.tasktop.dropwizard.launcher.launchConfigurationType";

	public static final String DROPWIZARD_SOURCEPATH_PROVIDER = "com.tasktop.dropwizard.launcher.sourcepathProvider"; //$NON-NLS-1$

	public static final String DROPWIZARD_CLASSPATH_PROVIDER = "com.tasktop.dropwizard.launcher.classpathProvider"; //$NON-NLS-1$

	public static final String ATTR_MODE = PLUGIN_ID + ".mode";

	public static final String ATTR_CONFIG_FILE = PLUGIN_ID + ".configFile";

	public static final String ATTR_CONFIG_FILE_PROJECT = PLUGIN_ID + ".configFileProject";

	private DropwizardLaunchConstants() {
		// prevent instantiation
	}

}
