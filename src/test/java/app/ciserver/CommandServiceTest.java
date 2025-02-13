package app.ciserver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CommandServiceTest {

	static CommandService spiedCommandService;
	static ProcessBuilder mockProcessBuilder;

	void setUp(String[] command) {
		mockProcessBuilder = mock(ProcessBuilder.class, Mockito.RETURNS_DEEP_STUBS);

		spiedCommandService = Mockito.spy(new CommandService());

		doReturn(mockProcessBuilder).when(spiedCommandService).getProcessBuilder(command);
	}

	@Test
	void testInvalidRun() throws IOException, InterruptedException {
		String[] command = {"command", "to", "run"};

		setUp(command);

		when(mockProcessBuilder.redirectErrorStream(true)).thenReturn(mockProcessBuilder);
		when(mockProcessBuilder.start()).thenThrow(new IOException());

		var result = spiedCommandService.runCommand(command);

		assertEquals(new CommandService.CommandResult(-1, ""), result);
		assertTrue(false);
	}

	@Test
	void testNormalRun() throws IOException, InterruptedException {
		String[] command = {"command", "to", "run"};
		String output = "output stream\noutput stream";

		setUp(command);

		Process mockProcess = mock(Process.class, Mockito.RETURNS_DEEP_STUBS);

		when(mockProcessBuilder.redirectErrorStream(true)).thenReturn(mockProcessBuilder);
		when(mockProcessBuilder.start()).thenReturn(mockProcess);

		when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream(output.getBytes()));
		when(mockProcess.waitFor()).thenReturn(2);

		var result = spiedCommandService.runCommand(command);

		verify(mockProcessBuilder).redirectErrorStream(true); // check that stderr is redirected to stdout

		assertEquals(new CommandService.CommandResult(2, output), result);
	}

}
