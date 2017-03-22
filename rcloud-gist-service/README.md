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
1. An executable jar file created here: `./rcloud-rawgist-service/build/libs`
2. An rpm install file created here:  `./rcloud-rawgist-service/build/distributions`
3. An deb install file created here:  `./rcloud-rawgist-service/build/distributions`

## Installation

The redhat and debian install archives will install the application to
`/opt/rcloud-gist-service` and create an entry in the `/etc/init.d/` folder
which can be used to start and stop the service.

### User and Groups
The installation creates a user and group for the service called `rcloudgistservice` which the service runs as.

### Debian based systems
To install the service on debian based systems the following command can be used, you will need to use the correct name for the deb file for the version you are installing.

`sudo dpkg -i ./rcloud-rawgist-service_0.1.0-20170126123855_all.deb`

## Default ports
The service uses two ports, one for the gist api and the other for the service management functionality, these can be controlled in configuration. The management port is secured using basic auth.
* 13020 application port
* 13021 management port

## Service Configuration

Configuration is held within the `/opt/rcloud-gist-service/application.yml` file.
The following parameters are configurable:

| Property | Description | Default |
|----------|-------------|---------|
| `service.port` | The port that the gist api is accessible over | `13020` |
| `management.port` | The port that the service management api is accessible over | `13021` |
| `security.user.name` | The username that is required for basic auth access to the management port | `admin` |
| `security.user.password` | The username that is required for basic auth access to the management port | If not specified the password is generated at service startup and can be identified in the `/var/log/rcloud-rawgist-service/rcloud-rawgist-service-file.log` file. The following command can be used to find the password. `cat /var/log/rcloud-rawgist-service/rcloud-rawgist-service-file.log &#124; grep "Using default security"`. More information can be found on the [spring boot documentation.](http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-monitoring.html) |
| `gists.root` | The location that the gist repositories are stored | `/var/rcloud-gist-service/gists/` |
| `gists.lockTimeout` | The timeout to acquire a lock on the gist to prevent concurrent modification | `30` |
| `gists.keyserver.host` | The host for the session key server | `127.0.0.1` |
| `gists.keyserver.port` | The port for the session key server | `4301` |
| `gists.keyserver.realm` | The realm for the session key server | `rcloud` |
| `gists.keyserver.url` | The URL template for the session key server | `http://127.0.0.1:4301/valid?token={token}&realm={realm}` |

### JVM Configuration
The startup parameters for the JVM are stored in the conf file in the installation directory, this must have the same name as the jar file.


## Starting and stopping the service

An System V startup script is installed as part of the installation, this supports the following commands:

| Command | Example                              |
|---------|--------------------------------------|
| start   | `service rcloud-gist-service start`  |
| stop    | `service rcloud-gist-service stop`   |
| status  | `service rcloud-gist-service status` |
| restart | `service rcloud-gist-service restart`|

## Logging
The service uses [Logback](https://logback.qos.ch/), this is controlled by the
configuration file in the installation directory `/opt/rcloud-gist-service/logback.xml`, this configuration file can be updated and the changes will be reloaded to alter the log output. The service writes log files to `/var/log/rcloud-gist-service/` access to this folder is restricted to the `root` user and the `rcloudgistservice` user/group.

## TODO
* Switch to using the git repository for looking things up rather than the file system.
* Performance tests
* Caching
* Documentation
