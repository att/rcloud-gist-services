[![Build Status](https://travis-ci.org/MangoTheCat/rcloud-gist-services.svg?branch=master)](https://travis-ci.org/MangoTheCat/rcloud-gist-services)

# RCloud Gist Service



## Overview

The RCloud Gist Service is a Java based service for enabling gist access to various different backend storage systems.

## Building
### Requirements
* Java 1.8 or above.

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

## LICENSE & Copyright

Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
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
