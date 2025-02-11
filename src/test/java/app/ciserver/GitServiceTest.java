package app.ciserver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import app.ciserver.CommandService.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GitServiceTest {

	static GitService gitService;
	static CommandService commandService;

	@BeforeEach
	void addService() {
		commandService = mock(CommandService.class);
		gitService = new GitService(commandService);
	}

	@Test
	void checkoutFail() {
		String[] command = {"git", "-C", "repo", "checkout", "master"};

		when(commandService.runCommand(any())).thenReturn(new CommandResult(1, "error"));

		boolean result = gitService.checkout("repo", "master");

		verify(commandService).runCommand(command);

		assertFalse(result);
	}

	@Test
	void checkoutSuccess() {
		String[] command = {"git", "-C", "repo", "checkout", "master"};

		when(commandService.runCommand(any())).thenReturn(new CommandResult(0, "success"));

		boolean result = gitService.checkout("repo", "master");

		verify(commandService).runCommand(command);

		assertTrue(result);
	}

	@Test
	void cloneFail() {
		String[] command = {"git", "clone", "url", "destination"};

		when(commandService.runCommand(any())).thenReturn(new CommandResult(1, "error"));

		boolean result = gitService.clone("url", "destination");

		verify(commandService).runCommand(command);

		assertFalse(result);
	}

	@Test
	void cloneSuccess() {
		String[] command = {"git", "clone", "url", "destination"};

		when(commandService.runCommand(any())).thenReturn(new CommandResult(0, "success"));

		boolean result = gitService.clone("url", "destination");

		verify(commandService).runCommand(command);

		assertTrue(result);
	}
}