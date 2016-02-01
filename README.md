# Eclipse Dropwizard Tools 
[ ![Codeship Status for Tasktop/dropwizard-tools](https://codeship.com/projects/90854c80-ab14-0133-943f-4a9cc2c4d260/status?branch=master)](https://codeship.com/projects/131204)
Eclipse Tools to ease the development of [Dropwizard](http://www.dropwizard.io) applications. The following tools are included and described in more detail below:
* Dropwizard Application Launcher
* [YAML Editor](https://github.com/oyse/yedit)

## Dropwizard Application Launcher

![Plugin](https://cloud.githubusercontent.com/assets/289648/12718807/39405a6c-c8f1-11e5-8694-64681d828aa4.png)

Dropwizard applications can be launched as plain Java applications. The drawback here is that each instance launched will use the same port and therfor fail to start. The Dropwizard Application Launcher Plug-In has the following features:
* Eliminate all running instances before starting a new one (no more "Port is already in use!")
* Define configuration file to use for the Dropwizard instance
* Choose the mode to launch e.g. server and check

## YEdit 
Some YAMl is part of each Dropwizard application. The Dropwizard tools contain the awesome [YEdit](https://github.com/oyse/yedit) Plug-In to make YAML editign fun.

## License
All code in this repository is published under terms of the [Apache 2.0 Software License](http://www.apache.org/licenses/LICENSE-2.0). The included binaries from YEdit are published under the terms of the [Eclipse Public License v1.0](https://www.eclipse.org/legal/epl-v10.html).