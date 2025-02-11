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
	 * using the gradlew executable at "<b>projectDir</b> + /gradlew".
	 * <p>
	 * 
	 * @param projectDir
	 *            - Full path to gradle project.
	 * @return - The exit code and content from standard output and standard error.
	 */
	public CommandResult compile(String projectDir) {
		CommandResult result = commandService
				.runCommand(new String[]{projectDir + "/gradlew", "-p", projectDir, "build"});
		return result;
	}

}
