package com.tasktop.dropwizard.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.RuntimeProcess;

public class RunningInstanceEliminator {

	private final ILaunchConfiguration configuration;

	public RunningInstanceEliminator(ILaunchConfiguration configuration) {
		this.configuration = configuration;
	}

	public void eliminateRunningInstances() {
		try {
			terminateIfRunning();
		} catch (CoreException e) {
			DropwizardLaunchExceptionHandler.handle(e);
		}
	}

	private void terminateIfRunning() throws CoreException {
		IProgressMonitor monitor = new NullProgressMonitor();
		String taskName = "Eliminate running instance";
		monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
		try {
			final ILaunch runningLaunch = findRunning();
			if (runningLaunch != null) {
				terminate(runningLaunch);
			}
		} finally {
			monitor.done();
		}
	}

	private void terminate(final ILaunch previousLaunch) throws DebugException {
		final Object signal = new Object();
		final boolean[] terminated = { false };
		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		debugPlugin.addDebugEventListener(new IDebugEventSetListener() {

			@Override
			public void handleDebugEvents(final DebugEvent[] events) {
				for (int i = 0; i < events.length; i++) {
					DebugEvent event = events[i];
					if (isTerminateEventFor(event, previousLaunch)) {
						DebugPlugin.getDefault().removeDebugEventListener(this);
						synchronized (signal) {
							terminated[0] = true;
							signal.notifyAll();
						}
					}
				}
			}
		});
		previousLaunch.terminate();
		try {
			synchronized (signal) {
				if (!terminated[0]) {
					signal.wait();
				}
			}
		} catch (InterruptedException e) {
			// ignore
		}
	}

	private boolean isTerminateEventFor(DebugEvent event, ILaunch launch) {
		boolean result = false;
		if (event.getKind() == DebugEvent.TERMINATE && event.getSource() instanceof RuntimeProcess) {
			RuntimeProcess process = (RuntimeProcess) event.getSource();
			result = process.getLaunch() == launch;
		}
		return result;
	}

	private ILaunch findRunning() {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] runningLaunches = launchManager.getLaunches();
		ILaunch result = null;
		for (ILaunch runningLaunch : runningLaunches) {
			if (configuration.getName().equals(getLaunchName(runningLaunch)) && !runningLaunch.isTerminated()) {
				result = runningLaunch;
			}
		}
		return result;
	}

	private String getLaunchName(ILaunch launch) {
		ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
		// the launch config might be null (e.g. if deleted) even though there
		// still exists a launch for that config  
		return launchConfiguration == null ? null : launchConfiguration.getName();
	}

}
