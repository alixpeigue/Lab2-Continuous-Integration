package app.ciserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javafx.util.Pair;

public class CommandService {
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
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true); // merge standard output and standard error streams.

		// Variables for return values.
		StringBuilder output = new StringBuilder();
		int exitCode = -1;
		// Read output from process
		try {
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String s = null;
			while ((s = reader.readLine()) != null) {
				output.append(s);
			}
			exitCode = process.waitFor();

		} catch (IOException | InterruptedException e) {
			System.out.println(e); // Could be changed in a later version
		}

		return new Pair<Integer, String>(exitCode, output.toString());

	}
}
