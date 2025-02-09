package app.ciserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HookController {

	private static final Logger logger = LoggerFactory.getLogger(HookController.class);

	private final GitService gitService;
	private final CompilationService compilationService;
	private final ObjectMapper objectMapper;

	public HookController(GitService gitService, CompilationService compilationService) {
		this.gitService = gitService;
		this.compilationService = compilationService;
		this.objectMapper = new ObjectMapper();
	}

	public void hookHandler(Context ctx) {
		try {
			// Step 1: Parse the request body to get the GitHub payload
			String requestBody = ctx.body();
			WebhookPayload payload = objectMapper.readValue(requestBody, WebhookPayload.class);

			// Step 2: Validate payload
			if (payload.getRepository() == null || payload.getRepository().getCloneUrl() == null
					|| payload.getRef() == null) {
				ctx.status(400).result("Invalid payload: missing required fields.");
				return;
			}

			// Step 3: Extract repository URL and branch name
			String repoUrl = payload.getRepository().getCloneUrl();
			String ref = payload.getRef();

			if (!ref.startsWith("refs/heads/")) {
				ctx.status(400).result("Unsupported ref type: " + ref);
				return;
			}
			String branchName = ref.split("/")[2];

			logger.info("Received webhook for branch: {} of repository: {}", branchName, repoUrl);

			// Step 4: Clone or update the repository
			String destinationFolder = "./ci-repos/" + branchName + "-" + System.currentTimeMillis();
			boolean cloneSuccess = gitService.clone(repoUrl, destinationFolder);

			if (!cloneSuccess) {
				ctx.status(500).result("Failed to clone the repository.");
				return;
			}

			// Step 5: Run compilation
			logger.info("Running compilation...");
			String compileOutput = compilationService.compile();

			if (compileOutput.contains("error") || compileOutput.contains("FAILURE")) {
				ctx.status(500).result("Compilation failed. Output:\n" + compileOutput);
				return;
			}

			// Step 6: Send response back to GitHub
			ctx.status(200).result("Webhook processed. Compilation output:\n" + compileOutput);

		} catch (Exception e) {
			logger.error("Error processing webhook: {}", e.getMessage(), e);
			ctx.status(500).result("Error processing webhook: " + e.getMessage());
		}
	}
}