/*******************************************************************************
 * Copyright (c) 2016 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Tasktop EULA
 * which accompanies this distribution, and is available at
 * http://tasktop.com/legal
 *******************************************************************************/

package com.tasktop.dropwizard.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

public class DropwizardLaunchDelegate extends JavaLaunchDelegate {

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		RunningInstanceEliminator instanceEliminator = new RunningInstanceEliminator(configuration);
		instanceEliminator.eliminateRunningInstances();
		return super.preLaunchCheck(configuration, mode, monitor);
	}

}
