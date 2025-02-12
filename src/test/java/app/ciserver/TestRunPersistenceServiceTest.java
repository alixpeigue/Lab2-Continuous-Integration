package app.ciserver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

class TestRunPersistenceServiceTest {

	private static final Date date;

	private static final String dateString;
	private static final String dateFormatString;
	static {
		dateFormatString = "yyyy-MM-dd HH:mm:ss";
		dateString = "11-02-2025 15:30:45";
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	@Spy
	private TestRunPersistenceService testRunPersistenceService;

	@BeforeEach
	void setUp() throws ParseException {
		MockitoAnnotations.openMocks(this);
	}

	/**
	 * When we load all the files, it should return a list of all the serialized
	 * contents for the files that haven't produces an {@code IOException}
	 */
	@Test
	void testLoadAll() throws IOException {
		TestRunModel run1 = new TestRunModel(date, "success", "COMMITSHA1", "main", "name", "Build success");
		TestRunModel run2 = new TestRunModel(new Date(), "success", "COMMITSHA2", "main", "name", "Build success");
		String fileContents1 = new ObjectMapper().writeValueAsString(run1);
		String fileContents2 = new ObjectMapper().writeValueAsString(run2);

		// We simulate a folder that contains two files
		doReturn(List.of("testRuns/file1.json", "testRuns/file2.json", "testRuns.file3.json"))
				.when(testRunPersistenceService).getAllJsonFilesInFolder();
		// One of them correctly returns its contents
		doReturn(fileContents1).when(testRunPersistenceService).getFileContents("testRuns/file1.json");
		doReturn(fileContents2).when(testRunPersistenceService).getFileContents("testRuns/file2.json");
		// The other produces an IOException
		doThrow(IOException.class).when(testRunPersistenceService).getFileContents("testRuns/file3.json");

		List<TestRunModel> result = testRunPersistenceService.loadAll();

		verify(testRunPersistenceService).getAllJsonFilesInFolder();
		verify(testRunPersistenceService).getFileContents("testRuns/file1.json");
		verify(testRunPersistenceService).getFileContents("testRuns/file2.json");

		// At the end, we should therefore only have one element corresponding to
		// file1.json
		assertEquals(2, result.size());
		assertEquals("COMMITSHA2", result.get(0).commitSHA()); // since run2 has a newer date that run1, they should be
																// in this order
		assertEquals("COMMITSHA1", result.get(1).commitSHA());
	}

	/**
	 * When the JSON file does not exist, {@code load} should return an empty
	 * {@code Optional}.
	 */
	@Test
	void testLoadFileDoesNotExists() throws IOException {
		String hash = "COMMITHASH";
		// We simulate the JSON file not existing
		doThrow(IOException.class).when(testRunPersistenceService).getFileContents(anyString());

		Optional<TestRunModel> result = testRunPersistenceService.load(hash);

		verify(testRunPersistenceService).getFileContents("testRuns/COMMITHASH.json");

		assertTrue(result.isEmpty());
	}

	/**
	 * When the file exists, {@code load} should return an {@code Optional}
	 * containing the deserialized contents of the file.
	 */
	@Test
	void testLoadFileExists() throws IOException {
		String hash = "COMMITHASH";
		TestRunModel expected = new TestRunModel(date, "success", "COMMITHASH", "main", "name", "Build success");
		String fileContents = new ObjectMapper().writeValueAsString(expected);
		doReturn(fileContents).when(testRunPersistenceService).getFileContents(anyString());

		Optional<TestRunModel> result = testRunPersistenceService.load(hash);

		verify(testRunPersistenceService).getFileContents("testRuns/COMMITHASH.json");

		assertTrue(result.isPresent());
		assertEquals(expected, result.get());
	}

	/**
	 * When we want to save a test run, {@code save} should return false if writing
	 * to disk produces an {@code IOExcpetion}
	 */
	@Test
	void testSaveFail() throws IOException {
		TestRunModel testRun = new TestRunModel(date, "success", "COMMITHASH", "main", "name", "Build success");
		String expectedFileContents = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(testRun);
		// We simulate writing to disk producing an IOException
		doThrow(IOException.class).when(testRunPersistenceService).writeToFile(anyString(), anyString());

		boolean result = testRunPersistenceService.save(testRun);

		verify(testRunPersistenceService).createFolder();
		verify(testRunPersistenceService).writeToFile("testRuns/COMMITHASH.json", expectedFileContents);
		assertFalse(result);
	}

	/**
	 * When we want to save a test run, {@code save} should return true if writing
	 * to disk does not produce an {@code IOExcpetion}
	 */
	@Test
	void testSaveSuccess() throws IOException {
		TestRunModel testRun = new TestRunModel(date, "success", "COMMITHASH", "main", "name", "Build success");
		String expectedFileContents = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(testRun);
		doNothing().when(testRunPersistenceService).writeToFile(anyString(), anyString());

		boolean result = testRunPersistenceService.save(testRun);

		verify(testRunPersistenceService).createFolder();
		verify(testRunPersistenceService).writeToFile("testRuns/COMMITHASH.json", expectedFileContents);
		assertTrue(result);
	}

}