package app.ciserver;

import app.ciserver.CommandService.CommandResult;
import io.javalin.http.Context;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HookController {

	private static final Logger logger = LoggerFactory.getLogger(HookController.class);

	private final GitService gitService;
	private final CompilationService compilationService;
	private final NotificationService notificationService;

	public HookController(GitService gitService, CompilationService compilationService,
			NotificationService notificationService) {
		this.gitService = gitService;
		this.compilationService = compilationService;
		this.notificationService = notificationService;
	}

	public void hookHandler(Context ctx) {
		try {
			// Step 1: Parse the request body into HookEventModel
			HookEventModel payload = ctx.bodyAsClass(HookEventModel.class);

			// Step 2: Extract repository URL and branch name
			String repoUrl = payload.repository().cloneUrl();
			Optional<String> branchName = payload.getBranchName();

			if (branchName.isEmpty()) {
				ctx.status(400);
				ctx.result("Unsupported ref type: " + payload.ref());
				notificationService.notifyFailure("Unsupported ref type: " + payload.ref(), payload);
				return;
			}

			logger.info("Received webhook for branch: {} of repository: {}", branchName.get(), repoUrl);

			// Step 3: Notify GitHub that the build is pending
			notificationService.notifyPending("Cloning the repository ...", payload);

			// Step 4: Clone or update the repository
			String destinationFolder = "./ci-repos/" + branchName.get() + "-" + System.currentTimeMillis();
			boolean cloneSuccess = gitService.clone(repoUrl, destinationFolder);

			if (!cloneSuccess) {
				notificationService.notifyFailure("Failed to clone repository.", payload);
				ctx.status(500);
				ctx.result("Failed to clone the repository.");
				return;
			}

			// Step 5: Run compilation
			logger.info("Running compilation...");
			CommandResult compileOutput = compilationService.compile(destinationFolder);

			// Step 6: Notify GitHub of the build result
			if (compileOutput.output().contains("error") || compileOutput.output().contains("FAILURE")
					|| compileOutput.exitCode() != 0) {
				notificationService.notifyFailure("Compilation failed. See the output for details.", payload);
				ctx.status(500);
				ctx.result("Compilation failed. Output:\n" + compileOutput);
			} else {
				notificationService.notifySuccess("Build succeeded!", payload);
				ctx.status(200);
				ctx.result("Webhook processed. Compilation output:\n" + compileOutput);
			}

		} catch (Exception e) {
			logger.error("Error processing webhook: {}", e.getMessage(), e);
			ctx.status(500);
			ctx.result("Error processing webhook: " + e.getMessage());
		}
	}
}