package app.ciserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

public class TestRunModelTest {
	@Test
	public void deserializeNoError() throws IOException {
		// Correct JSON matching TestRunModel structure
		String json = """
				{
				                "timestamp": "11-02-2025 15:30:45",
				                "status": "success",
				                "commitSHA": "a1b2c3d4e5f6g7h8i9j0",
				                "branchName": "feature-branch",
				                "pusherName": "JohnDoe",
				                "buildLogs": "BUILD SUCCESSFUL Total time: 30s"
				}
				""";

		// Deserialize JSON into TestRunModel object
		TestRunModel testRun = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(json,
				TestRunModel.class);

		// Assertions to verify all fields are correctly set
		assertNotNull(testRun);
		assertEquals("success", testRun.status());
		assertEquals("a1b2c3d4e5f6g7h8i9j0", testRun.commitSHA());
		assertEquals("feature-branch", testRun.branchName());
		assertEquals("JohnDoe", testRun.pusherName());
		assertEquals("BUILD SUCCESSFUL Total time: 30s", testRun.buildLogs());

		LocalDateTime expectedTimestamp = LocalDateTime.parse("11-02-2025 15:30:45",
				DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

		assertEquals(expectedTimestamp, testRun.timestamp());
	}

	@Test
	public void testTestRunModelSerialization() throws Exception {
		// Create an instance of TestRunModel
		TestRunModel testRun = new TestRunModel(LocalDateTime.of(2025, 2, 11, 15, 30, 45), "success",
				"a1b2c3d4e5f6g7h8i9j0", "feature-branch", "JohnDoe", "BUILD SUCCESSFUL Total time: 30s");

		// Expected JSON structure
		String expectedJson = """
				{
				    "timestamp": "11-02-2025 15:30:45",
				    "status": "success",
				    "commitSHA": "a1b2c3d4e5f6g7h8i9j0",
				    "branchName": "feature-branch",
				    "pusherName": "JohnDoe",
				    "buildLogs": "BUILD SUCCESSFUL Total time: 30s"
				}
				""";

		// Serialize to JSON
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		String json = objectMapper.writeValueAsString(testRun);

		// Compare actual and expected JSON
		assertEquals(objectMapper.readTree(expectedJson), objectMapper.readTree(json));
	}
}
