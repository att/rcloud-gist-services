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
The project uses GitFlow, and the .

#### Contributing
* This project follows GitFlow, all development should be done on feature branches with pull requests to merge into the development branches.


#### IDE integration
#### Eclipse
There are two mechanisms to load the project into the Eclipse IDE:
1. Use the Eclipse gradle plugin [BuildShip](https://github.com/eclipse/buildship), this is an Eclipse plugin that understands gradle projects. BuildShip does not include syntax highlighting editor, you will have to install Groovy Eclipse plugin.
2. Use the gradle eclipse plugin (this is a plugin in the gradle build file that will generate the appropriate eclipse project). To generate the eclipse files run the following `gradlew eclipse`, you can then import the project in as an existing project. When you add a new dependency in you will need to run this again to regenerate the eclipse project files and then refresh the project in eclipse and it will pick up the new settings. If you want to just generate the eclipse files for a specific sub module then run the command `gradlew :store:eclipse` for the store sub project.

## Components

## LICENSE & Copyright
