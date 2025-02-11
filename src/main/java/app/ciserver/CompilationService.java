package app.ciserver;

import app.ciserver.CommandService.*;

public class CompilationService {
	private CommandService commandService;
	public CompilationService(CommandService commandService) {
		this.commandService = commandService;
	}

	/**
	 * Builds gradle project at <b>projectDir</b>
	 * <p>
	 * using the gradlew executable at <b>gradleDir</b>.
	 * <p>
	 * If any of the parameters are null the method assumes the gradlew executable
	 * is in the working directory.
	 * 
	 * @param gradleDir
	 *            - Full path to gradlew executable.
	 * @param projectDir
	 *            - Full path to gradle project.
	 * @return - The exit code and content from standard output and standard error.
	 */
	public CommandResult compile(String gradleDir, String projectDir) { //
		CommandResult result = commandService.runCommand(new String[]{gradleDir, "-p", projectDir, "build"});

		return result;
	}

}
