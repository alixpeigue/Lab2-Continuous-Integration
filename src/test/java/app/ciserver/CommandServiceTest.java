package app.ciserver;

import static org.junit.jupiter.api.Assertions.*;

import javafx.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandServiceTest {
	private CommandService commandService;
	@BeforeEach
	void setUp() {
		commandService = new CommandService();
	}
	@Test
	public void validCommand() {
		String[] command = {"powershell.exe", "-Command", "ls"}; // Valid command.
		Pair<Integer, String> output = commandService.runCommand(command);
		int expectedExitCode = 0; // Should return with no errors.
		int actualExitCode = output.getKey(); // get the exitcode
		String actualString = output.getValue(); // get output string.
		assertEquals(expectedExitCode, actualExitCode);
		assertNotNull(actualString);
	}

}
