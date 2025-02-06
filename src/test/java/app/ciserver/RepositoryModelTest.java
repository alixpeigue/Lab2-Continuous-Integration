package app.ciserver;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class RepositoryModelTest {
	@Test
	public void deserializeError() throws IOException {
		String json = """
				{
				    "full_name": "fullNameValue",
				    "other": "otherValue"
				}
				""";

		assertThrows(MismatchedInputException.class, () -> new ObjectMapper().readValue(json, RepositoryModel.class));
	}

	@Test
	public void deserializeNoError() throws IOException {
		String json = """
				    {
				        "clone_url": "cloneUrlValue",
						"full_name": "fullNameValue",
						"other": "otherValue"
					}
				""";

		RepositoryModel hookEvent = new ObjectMapper().readValue(json, RepositoryModel.class);

		assertEquals("cloneUrlValue", hookEvent.cloneUrl());
		assertEquals("fullNameValue", hookEvent.fullName());
	}

}