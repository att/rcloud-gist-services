# Implementation of the RCloud Gist Service.
A Java based service to provide GitHub gist functionality to RCloud.

## Building
The code uses the Gradle build system and includes the gradle wrapper in the root
of the project. The only requirement for building is a Java JDK version 7 or greater
and an internet connection, all dependencies will be downloaded for the build.

To build the software run the following command which runs the tests and generates
the artifacts.

`./gradlew build`

### Outputs
The build generates 3 artifacts
1. An executable jar file created here: `./rcloud=gist-service/build/libs`
2. An rpm install file created here:  `./rcloud=gist-service/build/distributions`
3. An deb install file created here:  `./rcloud=gist-service/build/distributions`

## Installation

The redhat and debian install archives will install the application to
`/opt/rcloud-gist-service` and create an entry in the `/etc/init.d/` folder
which can be used to start and stop the service.

### User and Groups
The installation creates a user and group for the service called `rcloudgistservice` which the service runs as.

### Debian based systems
To install the service on debian based systems the following command can be used, you will need to use the correct name for the deb file for the version you are installing.

`sudo dpkg -i ./rcloud-gist-service_0.1.0-20170126123855_all.deb`

## Default ports
The service uses two ports, one for the gist api and the other for the service management functionality, these can be controlled in configuration. The management port is secured using basic auth.

## Service Configuration

Configuration is held within the `/opt/rcloud-gist-service/application.yml` file.
The following parameters are configurable:

| Property | Description | Default |
|----------|-------------|---------|
| `github.api.url` | The URL to the root of the GitHub installation that this should use | `https://api.github.com` |
| `service.port` | The port that the gist api is accessible over | `13010` |
| `management.port` | The port that the service management api is accessible over | `13011` |
| `security.user.name` | The username that is required for basic auth access to the management port | `admin` |
| `security.user.password` | The username that is required for basic auth access to the management port | If not specified the password is generated at service startup and can be identified in the `/var/log/rcloud-gist-service/rcloud-gist-service-file.log` file. The following command can be used to find the password. `cat /var/log/rcloud-gist-service/rcloud-gist-service-file.log &#124; grep "Using default security"`. More information can be found on the [spring boot documentation.](http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-monitoring.html) |

### Java Configuration
The startup parameters for the JVM are stored in the conf file in the installation directory, this must have the same name as the jar file.


## RCloud Configuration
To configure RCloud edit the `rcloud.conf` and change the `api.github.url` value to the URL of this service if running with the defaults then this would be `http://localhost:13010` e.g. :

`github.api.url: http://localhost:13010`

## Starting and stopping the service

An System V startup script is installed as part of the installation, thissupports the following commands:

| Command | Example                              |
|---------|--------------------------------------|
| start   | `service rcloud-gist-service start`  |
| stop    | `service rcloud-gist-service stop`   |
| status  | `service rcloud-gist-service status` |

## Logging
The service uses [Logback](https://logback.qos.ch/), this is controlled by the
configuration file in the installation directory `/opt/rcloud-gist-service/logback.xml`, this configuration file can be updated and the changes will be reloaded to alter the log output. The service writes log files to `/var/log/rcloud-gist-service/` access to this folder is restricted to `root` group and the `rcloudgistservice` user/group.
