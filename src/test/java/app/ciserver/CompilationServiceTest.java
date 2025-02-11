package app.ciserver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import app.ciserver.CommandService.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CompilationServiceTest {

	static CompilationService compilationService;
	static CommandService commandService;

	@BeforeEach
	void addService() {
		commandService = mock(CommandService.class);
		compilationService = new CompilationService(commandService);
	}
	/**
	 * <p>
	 * Mock test to see that the command given to the compile method
	 * </p>
	 * <p>
	 * Is the same one called by commandService.runCommand()
	 * </p>
	 */
	@Test
	void invalidCompilation() {
		String[] command = {"invalid/path", "-p", "path/to/project", "build"};

		when(commandService.runCommand(any())).thenReturn(new CommandResult(1, "error"));

		CommandResult result = compilationService.compile("invalid/path", "path/to/project");

		verify(commandService).runCommand(command);

		assertEquals(1, result.exitCode());
	}

	@Test
	void validCompilation() {
		String[] command = {"path/to/gradlew", "-p", "path/to/project", "build"};

		when(commandService.runCommand(any())).thenReturn(new CommandResult(0, "success"));

		CommandResult result = compilationService.compile("path/to/gradlew", "path/to/project");

		verify(commandService).runCommand(command);

		assertEquals(0, result.exitCode());
	}

}
