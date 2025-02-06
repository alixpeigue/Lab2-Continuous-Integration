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

## Workflow
To add a feature or fix something:
1. Make an issue describing it
2. Make a branch of main or relevant branch if applicable
3. Work on the branch and once "done" make sure it builds (run ./gradlew clean build, if windows ./gradlew.bat clean build)
4. Make sure the tests pass when having ran ./gradlew clean build, then run ./gradlew spotlessApply to make sure formatting is correct!
5. Push up the changes to your branch and make a PR, do not merge until review has been done
6. Always one commit per PR, commits can be squashed though in case there are several
7. Commit message should be of the form: "feature/fix/doc/refactor: #issue-number Did the thing that makes the PR relevant"
8. If your PR fixes an issue, make sure to [link the issue that you are fixing](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/linking-a-pull-request-to-an-issue)
## Statement of contributions
TODO
## Statement of Essence
TODO
