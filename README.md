# Continuous Integration server using Javalin
dummy   

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

## Servers testing functionality

The server when triggered will clone the repo and run ./gradlew build which will check if the code compiles, run the tests and check if formatting is correct. Depending on the result of this operation, the server will then send a commit status via the GitHub REST API
The notification service sends and HTTP POST request to GitHub to inform about the status of the run. They are exposed as public interfaces for each status of a run as given in the GitHub REST API. 
Mockito has been extensively used for most services to be able to test whether the correct functionality has been implemented, the right methods have been called in correct order and the results are in expectation for given inputs.

## Libraries

The app uses Javalin as its web framework. Jackson is used for serialization/deserialization,
the template engine is Thymeleaf and slf4j-simple is used for logging.

For unit tests, Mockito for mocking the injected dependencies in the services.

## Workflow
Additional details available in [wiki](https://github.com/alixpeigue/Lab2-Continuous-Integration/wiki/Development-practices), to add a feature or fix something:
1. Make an issue describing it
2. Make a branch of main or relevant branch if applicable
3. Work on the branch and once "done" make sure it builds (run ./gradlew clean build, if windows ./gradlew.bat clean build)
4. Make sure the tests pass when having ran ./gradlew clean build, then run ./gradlew spotlessApply to make sure formatting is correct!
5. Push up the changes to your branch and make a PR, do not merge until review has been done
6. Always one commit per PR, commits can be squashed though in case there are several
7. Commit message should be of the form: "feature/fix/doc/refactor: #issue-number Did the thing that makes the PR relevant"
8. If your PR fixes an issue, make sure to [link the issue that you are fixing](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/linking-a-pull-request-to-an-issue)
## Statement of contributions
* Alix Peigue: Repo setup, CommandService 
* Samer Jameel: CommitStatusModel
* Leo Lundberg: NotificationService  
* Adam Fridén Rasmussen: Compilation and TestService and Persistence
* Anass Inani: Hook controller, Persistence
* Everyone participated in code reviews to varying degrees
## Statement of Essence
State : Foundation established

Compared to Lab 1, we have reached this state through us documenting explicitly how contributions should be made and named, and also we have added automation to our toolchain that enforces adherence to some of our documented conventions.
- We have a {wiki](https://github.com/alixpeigue/Lab2-Continuous-Integration/wiki/Development-practices)
- Clear formatting conventions that are automatically checked at build time
- GitHub Actions set up to catch any violations and ensure compliance

That said though, we have not progressed forward to "In Use" because of:
- There is no documented way to give feedback on the work and practices.
- The tools and practices are not regularly inspected

