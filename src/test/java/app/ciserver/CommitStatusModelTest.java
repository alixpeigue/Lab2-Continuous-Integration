package app.ciserver;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CommitStatusModelTest {
	@Test
	public void deserializeNoError() throws IOException {
		// Correct JSON matching CommitStatusModel.CommitStatus structure
		String json = """
				{
				    "state": "success",
				    "description": "Build completed successfully",
				    "context": "CI Server"
				}
				""";

		// Deserialize JSON into CommitStatus object
		CommitStatusModel commitStatus = new ObjectMapper().readValue(json,
				CommitStatusModel.class);

		// Assertions to verify all fields are correctly set
		assertEquals("success", commitStatus.state());
		assertEquals("Build completed successfully", commitStatus.description());
		assertEquals("CI Server", commitStatus.context());

		// Ensure that object is not null
		assertNotNull(commitStatus);
	}

	@Test
	public void testCommitStatusSerialization() throws Exception {
		// Create an instance of CommitStatus
		CommitStatusModel commitStatus = new CommitStatusModel("success",
				"Build completed successfully", "CI Server");

		// Expected JSON (field order doesn't matter, but structure should match)
		String expectedJson = """
				{"state":"success","description":"Build completed successfully","context":"CI Server"}
				""";

		// Compare actual and expected JSON
		String json = new ObjectMapper().writeValueAsString(commitStatus);

		assertEquals(new ObjectMapper().readTree(expectedJson), new ObjectMapper().readTree(json));
	}

}
