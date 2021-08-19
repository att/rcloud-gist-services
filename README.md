[![Build Status](https://travis-ci.org/MangoTheCat/rcloud-gist-services.svg?branch=master)](https://travis-ci.org/MangoTheCat/rcloud-gist-services)

# RCloud Gist Service

## Overview

The RCloud Gist Service is a Java based service for enabling gist access to various different backend storage systems.

## Building
### Requirements
* Java 1.7 or above.

### Building
The project uses the gradle build system and contains the gradle wrapper script
which will automatically obtain the specified version of gradle. Building the
software for the first time maybe slow.

#### Build commands
* Build the whole project: `gradlew build`
* Cleaning the project: `gradlew clean`
* Running build with reporting: `gradlew clean build generateProjectReports`

#### Versioning
The project uses GitFlow.

#### Contributing
* This project follows GitFlow, all development should be done on feature branches with pull requests to merge into the development branches.

#### IDE integration
#### Eclipse
There are two mechanisms to load the project into the Eclipse IDE:
1. Use the Eclipse gradle plugin [BuildShip](https://github.com/eclipse/buildship), this is an Eclipse plugin that understands gradle projects. BuildShip does not include syntax highlighting editor, you will have to install Groovy Eclipse plugin.
2. Use the gradle eclipse plugin (this is a plugin in the gradle build file that will generate the appropriate eclipse project). To generate the eclipse files run the following `gradlew eclipse`, you can then import the project in as an existing project. When you add a new dependency in you will need to run this again to regenerate the eclipse project files and then refresh the project in eclipse and it will pick up the new settings. If you want to just generate the eclipse files for a specific sub module then run the command `gradlew :store:eclipse` for the store sub project.


## Vagrant
The Vagrantfile sets up 80% of the environment needed to run rcloud. It takes a long time to finish the provisioning. Once done rcloud will be in `/opt/rcloud/rcloud-1.7/` you will need to setup the `rcloud.conf` file and then call `sudo ./scripts/fresh_start.sh`. The `bootstrapR.sh` has already been called as part of the provisioning.

## Components
### RCloud Gist Proxy Service
A proxy service that routes requests from rcloud conditionally between GitHub
and the RCloud Gist Service.

### RCloud Gist Service
An lite implementation of the GitHub gist API.

### Session Key Server Lib
Java library implementing an integration with Spring Security and the RCloud
Session Key Server.

## Installation of the gist service
The following installation instructions assume that you have rcloud installed,
and that you have the RCloud SessionKeyServer installed, if not then please
ensure that are installed first.

1. Ensure that Java is installed, the minimum version is Java 7.
1. Download the rcloud-gist-service from the [GitHub repository](https://github.com/MangoTheCat/rcloud-gist-services/releases) for your platform.
1. Install the archive using the appropriate tool e.g. for debian `sudo dpkg -i rcloud-gist-service_0.2.0-20170126172521_all.deb`
1. Start the rcloud-gist-service `sudo service rcloud-gist-service start`
1. Update the rcloud.conf to point to the proxy service, set the `github.api.url` value to `http://localhost:13020/` e.g. `github.api.url: http://localhost:13020/`
1. Start RCloud

The above instructions are almost identical for the proxy service just using `gistproxy` instead of `gist`.

The services will have started up with their default configuration. If a different configuration is required, the configuration files can be found in `/opt/rcloud-gist-service/` and `/opt/rcloud-gistproxy-service/`

## LICENSE & Copyright

Copyright (c) 2017-2018 AT&T Intellectual Property, [http://www.att.com]
SPDX-License-Identifier:   MIT

MIT License

Copyright (c) 2017 AT&T Intellectual Property

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.


