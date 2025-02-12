# Continuous Integration server using Javalin
## Running and testing 

To build `./gradlew build`

To run, `./gradlew run` runs the server in dev environment

To test `./gradlew test`

The codded is formatted using spotless, `./gradlew spotlessApply` to format the project files.
Building will fail if the files are not formatted correctly.

## Deploying on a server

To deploy, generate the jar file `./gradlew shadowJar`, 
generates the file `ciserver-1.0-SNAPSHOT-all.jar` in `build/libs`.

The file can be distributed and ran in prod environment via `java -jar ciserver-1.0-SNAPSHOT-all.jar`.

You can also choose the environment by setting the JAVA_ENV environment variable to `dev` or `prod` (default: `prod`).

## Libraries

The app uses Javalin as its web framework. Jackson is used for serialization/deserialization,
the template engine is Thymeleaf and slf4j-simple is used for logging.

For unit tests, Mockito for mocking the injected dependencies in the services.
