package app.ciserver;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class HookEventModelTest {
	@Test
	public void deserializeError() throws IOException {
		String json = """
				{
				    "before": "beforeValue",
				    "ref": "refValue",
				    "pusher": {
				        "email": "emailValue",
				        "name": "nameValue",
				        "username": "usernameValue"
				    },
				    "repository": {
				    	"clone_url": "cloneUrlValue",
				    	"full_name": "fullNameValue"
				    }
				}
				""";

		assertThrows(MismatchedInputException.class, () -> new ObjectMapper().readValue(json, HookEventModel.class));
	}

	@Test
	public void deserializeNoError() throws IOException {
		String json = """
				{
				    "after": "afterValue",
				    "before": "beforeValue",
				    "ref": "refValue",
				    "pusher": {
				        "email": "emailValue",
				        "name": "nameValue",
				        "username": "usernameValue"
				    },
				    "repository": {
				    	"clone_url": "cloneUrlValue",
				    	"full_name": "fullNameValue"
				    }
				}
				""";

		HookEventModel hookEvent = new ObjectMapper().readValue(json, HookEventModel.class);

		assertEquals("afterValue", hookEvent.after());
		assertEquals("refValue", hookEvent.ref());
		assertNotNull(hookEvent.pusher());
		assertNotNull(hookEvent.repository());
	}

	@Test
	public void getBranchNameExisting() {
		HookEventModel hookEvent = new HookEventModel(null, "refs/heads/branch_name", null, null);

		Optional<String> branchName = hookEvent.getBranchName();

		assertTrue(branchName.isPresent());
		assertEquals("branch_name", branchName.get());
	}

	@Test
	public void getBranchNameNotExisting() {
		HookEventModel hookEvent = new HookEventModel(null, "refs/tags/v0.0.1", null, null);

		Optional<String> branchName = hookEvent.getBranchName();

		assertTrue(branchName.isEmpty());
	}
}