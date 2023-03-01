## Workshop Setup
This file can be used to check that you have correctly installed everything that you need for the workshop.

If something does not work, please check the [troubleshooting section of `HELP.md`](HELP.md#troubleshooting). You can also contact us at [`slack.micrometer.io`](https://slack.micrometer.io/) in the [`#springio2024`](https://micrometer-metrics.slack.com/archives/C074GUZHA31) channel.

### Java
You will need at least Java 17 to run this workshop, but we will use Java 21 since it's the latest LTS.

We recommend using SDKMAN! to manage installed Java versions, but you're free to use whatever you prefer as long as at least Java 17 is available. On Windows, you will need bash to use SDKMAN! (Git Bash, MinGW, Cygwin, WSL, etc.), if you don't want to install it you can try CHOCOLATEY! too.


#### SDKMAN!
The following instructions can be used if you are using SDKMAN!

1. Install SDKMAN! for your system by following instructions at https://sdkman.io/
2. Install Java by typing `sdk install java 23.1.2.r21-nik`

Check the Java version:

```shell
sdk use java 23.1.2.r21-nik
java --version
```

You should see a similar output:

```
openjdk 21.0.2 2024-01-16 LTS
OpenJDK Runtime Environment Liberica-NIK-23.1.2-1 (build 21.0.2+14-LTS)
OpenJDK 64-Bit Server VM Liberica-NIK-23.1.2-1 (build 21.0.2+14-LTS, mixed mode, sharing)
```



#### CHOCOLATEY! (Windows)
The following instructions can be used if you are using CHOCOLATEY!

1. Install CHOCOLATEY! for your system by following instructions at https://community.chocolatey.org/
2. Install Java by running the following in a PowerShell as Administrator:

```shell
choco install graalvm-java21
```

Check that you can use Java:


1. Open `System Properties`
2. Click `Environment Variables...` and verify the `PATH` and `JAVA_HOME` System Variables to use the right JDK paths
3. Open a new PowerShell window and check the Java version
```shell
java --version
```

#### Without SDKMAN! or CHOCOLATEY!
If you prefer not to use SKDMAN! or CHOCOLATEY! you can install Java whichever way you're most comfortable with.
As long as you have at least Java 17, you should be fine.

Please check your installation by checking the output of `java --version`:

```shell
java --version
```

You should see a similar output:

```
openjdk 21.0.2 2024-01-16 LTS
OpenJDK Runtime Environment Liberica-NIK-23.1.2-1 (build 21.0.2+14-LTS)
OpenJDK 64-Bit Server VM Liberica-NIK-23.1.2-1 (build 21.0.2+14-LTS, mixed mode, sharing)
```



### Maven
This workshop uses `mvnw` so you shouldn't need to install Maven directly.

To check that Maven works run the following command from this directory.

On Linux/Mac

```shell
./mvnw --version
```

On Windows

```shell
mvnw.cmd --version
```

If you want to ensure your local Maven cache has most of the files we'll need, you can build the pom files in this directory:

```shell
./mvnw package
```

You should see a "success" message after these command:
```
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```



### Docker Compose
We will be using Docker Compose to set up things like Postgres which we will need for the workshop.
We recommend Docker Desktop if possible, but any Docker installation that can run `docker compose` should work fine.

Docker Desktop is free for educational purposes.

Please install Docker from https://www.docker.com or using your favorite open source distribution.

To check docker is running, you can run:

```shell
docker run --rm hello-world
```

To check docker compose is working, run the following from this directory:

```shell
docker compose -f docker-compose-hello.yml up
```

Once you see the "hello world" output, you can destroy the containers using:

```shell
docker compose -f docker-compose-hello.yml down
```

TIP: Depending on your installation, you might need to use the command `docker-compose` rather than `docker compose` (this usually means that you are using an older version, please consider upgrading).

If the hello world example works, then you can try starting the full docker compose configuration that we'll be using:

```shell
docker compose up
```

NOTE: it will take some time to initially download images and start the containers. It will help to do this with a fast internet connection.

Check that the following links work:

http://localhost:9090 (Prometheus)
http://localhost:3000 (Grafana)

If everything works you can hit `ctrl-c` to stop docker compose, and you can destroy the containers using:

```shell
docker compose down
```



### HTTPie
We'll need to make some REST calls to our application.
You can use whatever tool you like, but if you have no preference we recommend HTTPie.

You can install it from https://httpie.io/



### IDE
You can use any Java IDE you like to work with the code, just check that it can work with Maven projects.

Please import the `dog-service` project.
