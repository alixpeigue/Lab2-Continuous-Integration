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
	/**
	 * Handles incoming GitHub webhooks and triggers the CI/CD pipeline.
	 * 
	 * This method processes webhook payloads, extracts repository details, clones
	 * the repository, compiles the project, and sends notifications.
	 *
	 *
	 * @param ctx
	 *            The Javalin HTTP context containing the webhook request.
	 **/
	public void hookHandler(Context ctx) {

		// Step 1: Parse the request body into HookEventModel
		HookEventModel payload = ctx.bodyAsClass(HookEventModel.class);

		// Step 2: Extract repository URL and branch name
		String repoUrl;
		Optional<String> branchName;
		try {
			repoUrl = payload.repository().cloneUrl();
			branchName = payload.getBranchName();
		} catch (NullPointerException e) {
			logger.error("Repository information is missing in webhook: {}", e.getMessage(), e);
			ctx.status(200);
			ctx.result("Invalid webhook payload: Missing repository information.");
			return;
		}

		if (branchName.isEmpty()) {
			ctx.status(200);
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
			ctx.status(200);
			ctx.result("Failed to clone the repository.");
			return;
		}

		// Checkout the branch after cloning
		boolean checkoutSuccess = gitService.checkout(destinationFolder, payload.after());
		if (!checkoutSuccess) {
			notificationService.notifyFailure("Failed to checkout branch.", payload);
			ctx.status(200);
			ctx.result("Failed to checkout the branch.");
			return;
		}

		// Step 5: Run compilation
		logger.info("Running compilation...");
		CommandResult compileOutput = compilationService.compile(destinationFolder);

		// Step 6: Notify GitHub of the build result
		if (compileOutput.exitCode() != 0) {
			notificationService.notifyFailure("Compilation failed. See the output for details.", payload);
			ctx.status(200);
			ctx.result("Compilation failed. Output:\n" + compileOutput);
		} else {
			notificationService.notifySuccess("Build succeeded!", payload);
			ctx.status(200);
			ctx.result("Webhook processed. Compilation output:\n" + compileOutput);
		}

	}
}