package app.ciserver;

import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRunPersistenceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestRunPersistenceService.class);

	List<String> getAllJsonFilesInFolder() {
		File[] files = new File("testRuns").listFiles();

		if (files == null) {
			return new ArrayList<>();
		}
		List<String> result = new ArrayList<>();
		for (File file : files) {
			if (file.isFile() && file.getName().endsWith(".json")) {
				result.add(file.getName());
			}
		}
		return result;
	}

	String getFileContents(String filename) throws IOException {
		return readString(Path.of(filename), StandardCharsets.UTF_8);
	}

	/**
	 * Get a test run from disk given a commit hash
	 * 
	 * @param commitSHA
	 *            hash of the commit
	 * @return An empty optional if not test run with the corresponding hash was
	 *         found, the test run is present otherwise.
	 */
	public Optional<TestRunModel> load(String commitSHA) {
		String filename = "testRuns/" + commitSHA + ".json";
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			TestRunModel testRun = objectMapper.readValue(getFileContents(filename), TestRunModel.class);
			return Optional.of(testRun);
		} catch (StreamReadException | DatabindException e) { // File format error
			LOGGER.error("Error deserializing from file '{}': {}", filename, e.getMessage());
			return Optional.empty();
		} catch (IOException e) { // File does not exist
			return Optional.empty();
		}
	}

	/**
	 * Load all the previous test runs currently saved on disk
	 * 
	 * @return a list of all the test runs found on disk
	 */
	public List<TestRunModel> loadAll() {
		List<TestRunModel> testRuns = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		for (String fileName : getAllJsonFilesInFolder()) {
			try {
				testRuns.add(objectMapper.readValue(getFileContents(fileName), TestRunModel.class));
			} catch (IOException e) {
				LOGGER.error("While getting all test runs, error deserializing from file '{}': {}", fileName,
						e.getMessage());
			}
		}
		return testRuns;
	}

	/**
	 * Saves the test run on the disk
	 * 
	 * @param testRun
	 *            the test run to save
	 * @return true if the test run was saved successfully, false otherwise
	 */
	public boolean save(TestRunModel testRun) {
		String filename = "testRuns/" + testRun.commitSHA() + ".json";
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			writeToFile(filename, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(testRun));
		} catch (IOException e) {
			LOGGER.error("Could not save file '{}': {}", filename, e.getMessage());
			return false;
		}
		return true;
	}

	void writeToFile(String filename, String contents) throws IOException {
		writeString(Path.of(filename), contents);
	}
}
