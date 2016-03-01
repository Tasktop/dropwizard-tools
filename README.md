# Eclipse Dropwizard Tools ![Logo](https://cloud.githubusercontent.com/assets/289648/12719167/c63235f6-c8f3-11e5-87e4-124451a26fdf.png) [ ![Codeship Status for Tasktop/dropwizard-tools](https://codeship.com/projects/90854c80-ab14-0133-943f-4a9cc2c4d260/status?branch=master)](https://codeship.com/projects/131204) [![License](http://img.shields.io/badge/license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
Eclipse Tools to ease the development of [Dropwizard](http://www.dropwizard.io) applications. The following tools are included and described in more detail below:
* Dropwizard Application Launcher
* [YAML Editor](https://github.com/oyse/yedit)

## Dropwizard Application Launcher

![Plugin](https://cloud.githubusercontent.com/assets/289648/12744330/e49662c4-c993-11e5-8ecd-a6ac58183d79.png)

Dropwizard applications can be launched as plain Java applications. The drawback here is that each instance launched will use the same port and therefore new instances fail to start. The Dropwizard Application Launcher solves this and has the following features:
* Eliminate all running instances before starting a new one (no more "Port is already in use!")
* Define configuration file to use for the Dropwizard instance
* Choose the mode to launch e.g. server and check
* Select an Application class (must implement `main` method) and Run As... Dropwizard Application ![Select](https://cloud.githubusercontent.com/assets/289648/12719099/4d55d020-c8f3-11e5-99e1-44747b136d5e.png)

## YEdit 
Some YAML is part of each Dropwizard application. The Dropwizard tools contain the awesome [YEdit](https://github.com/oyse/yedit) Plug-In to make YAML editing fun.

## Installation

* Open the Eclipse help menu
* Click "Install New Software"
* Enter this URL: http://tasktop.github.io/dropwizard-tools/
* Select the Features you wish to install
* Click on "Finish", accept the license
* Enjoy the tools!

## License
All code in this repository is published under terms of the [Apache 2.0 Software License](http://www.apache.org/licenses/LICENSE-2.0). The included binaries from YEdit are published under the terms of the [Eclipse Public License v1.0](https://www.eclipse.org/legal/epl-v10.html).
