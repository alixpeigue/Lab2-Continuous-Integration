package app.ciserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code CommandService} is used to simplify the handling and launching of
 * commands.
 */
public class CommandService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandService.class);

	/**
	 * <b>DON'T USE THIS METHOD, IT IS PACKAGE-PRIVATE ONLY FOR TESTING</b>
	 * <p>
	 * Gets a process builder for the command
	 * </p>
	 * 
	 * @param command
	 *            the command to run
	 * @return the process builder
	 */
	ProcessBuilder getProcessBuilder(String[] command) {
		return new ProcessBuilder(command);
	}

	/**
	 * Creates a process and runs the command given by the <b>command</b> parameter.
	 * <p>
	 * If the process finishes the exit code and output is returned.
	 * <p>
	 * For example to execute command "gradle build" the input should be ["gradle",
	 * "build"]
	 * 
	 * @param command
	 *            - String representation of command that should be executed.
	 * @return - A pair consisting of the exit code and the ouput from the process.
	 */
	public Pair<Integer, String> runCommand(String[] command) {
		// Create process from command
		ProcessBuilder processBuilder = getProcessBuilder(command);
		processBuilder.redirectErrorStream(true); // merge standard output and standard error streams.

		// Variables for return values.
		StringBuilder output = new StringBuilder();
		int exitCode = -1;
		// Read output from process
		try {
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String s;
			// Read each line, add them to the builder, separated by a newline character
			if ((s = reader.readLine()) != null) {
				output.append(s);
			}
			while ((s = reader.readLine()) != null) {
				output.append("\n").append(s);
			}

			exitCode = process.waitFor();

		} catch (IOException | InterruptedException e) {
			LOGGER.error("Error while executing command '{}' : {}", String.join(" ", command), e.getMessage());
		}

		return new Pair<>(exitCode, output.toString());

	}
}
