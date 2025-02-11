package app.ciserver;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class PusherModelTest {
	@Test
	public void deserializeError() throws IOException {
		String json = """
				    {
						"name": "nameValue",
						"other": "otherValue"
					}
				""";

		assertThrows(MismatchedInputException.class, () -> new ObjectMapper().readValue(json, PusherModel.class));
	}

	@Test
	public void deserializeNoError() throws IOException {
		String json = """
				    {
				        "email": "emailValue",
						"name": "nameValue",
						"other": "otherValue"
					}
				""";

		PusherModel pusher = new ObjectMapper().readValue(json, PusherModel.class);

		assertEquals("emailValue", pusher.email());
		assertEquals("nameValue", pusher.name());
	}
}