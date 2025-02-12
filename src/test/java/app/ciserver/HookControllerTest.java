package app.ciserver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.javalin.http.Context;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class HookControllerTest {

	private HookController hookController;

	@Mock
	private GitService gitService;

	@Mock
	private CompilationService compilationService;

	@Mock
	private NotificationService notificationService;

	@Mock
	private Context ctx;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		hookController = new HookController(gitService, compilationService, notificationService);
	}

	@Test
	void testHookHandler_CompilationFailure() {
		// Arrange
		HookEventModel mockPayload = mock(HookEventModel.class);
		when(mockPayload.repository())
				.thenReturn(new RepositoryModel("https://github.com/example/repo.git", "example/repo"));
		when(mockPayload.getBranchName()).thenReturn(Optional.of("main"));
		when(ctx.bodyAsClass(HookEventModel.class)).thenReturn(mockPayload);

		when(gitService.clone(anyString(), anyString())).thenReturn(true);
		when(gitService.checkout(anyString(), anyString())).thenReturn(true); // Ensure checkout succeeds
		when(compilationService.compile(anyString()))
				.thenReturn(new CommandService.CommandResult(1, "FAILURE: Build failed"));

		// Act
		hookController.hookHandler(ctx);

		// Assert
		verify(notificationService).notifyFailure(contains("Compilation failed"), eq(mockPayload));
		verify(ctx).status(200); // Should return 200 even if compilation fails
	}

	@Test
	void testHookHandler_FailedCheckout() {
		// Arrange
		HookEventModel mockPayload = mock(HookEventModel.class);
		when(mockPayload.repository())
				.thenReturn(new RepositoryModel("https://github.com/example/repo.git", "example/repo"));
		when(mockPayload.getBranchName()).thenReturn(Optional.of("main"));
		when(ctx.bodyAsClass(HookEventModel.class)).thenReturn(mockPayload);

		when(gitService.clone(anyString(), anyString())).thenReturn(true);
		when(gitService.checkout(anyString(), anyString())).thenReturn(false); // Mock checkout failure

		// Act
		hookController.hookHandler(ctx);

		// Assert
		verify(notificationService).notifyFailure(contains("Failed to checkout branch"), eq(mockPayload));
		verify(ctx).status(200);
		verify(ctx).result(contains("Failed to checkout the branch."));
	}

	@Test
	void testHookHandler_FailedClone() {
		// Arrange
		HookEventModel mockPayload = mock(HookEventModel.class);
		when(mockPayload.repository())
				.thenReturn(new RepositoryModel("https://github.com/example/repo.git", "example/repo"));
		when(mockPayload.getBranchName()).thenReturn(Optional.of("main"));
		when(ctx.bodyAsClass(HookEventModel.class)).thenReturn(mockPayload);

		when(gitService.clone(anyString(), anyString())).thenReturn(false);

		// Act
		hookController.hookHandler(ctx);

		// Assert
		verify(notificationService).notifyFailure(contains("Failed to clone repository"), eq(mockPayload));
		verify(ctx).status(200);
		verify(ctx).result(contains("Failed to clone the repository."));
	}

	@Test
	void testHookHandler_MissingBranchName() {
		// Arrange
		HookEventModel mockPayload = mock(HookEventModel.class);
		when(mockPayload.repository())
				.thenReturn(new RepositoryModel("https://github.com/example/repo.git", "example/repo"));
		when(mockPayload.getBranchName()).thenReturn(Optional.empty());
		when(ctx.bodyAsClass(HookEventModel.class)).thenReturn(mockPayload);

		// Act
		hookController.hookHandler(ctx);

		// Assert
		verify(ctx).status(400);
		verify(notificationService).notifyFailure(contains("Unsupported ref type"), eq(mockPayload));
	}

	@Test
	void testHookHandler_SuccessfulBuild() {
		// Arrange
		HookEventModel mockPayload = mock(HookEventModel.class);
		RepositoryModel mockRepo = mock(RepositoryModel.class);

		when(mockPayload.repository()).thenReturn(mockRepo);
		when(mockRepo.cloneUrl()).thenReturn("https://github.com/example/repo.git");
		when(mockPayload.getBranchName()).thenReturn(Optional.of("main"));
		when(ctx.bodyAsClass(HookEventModel.class)).thenReturn(mockPayload);

		// Ensure both clone and checkout succeed
		when(gitService.clone(anyString(), anyString())).thenReturn(true);
		when(gitService.checkout(anyString(), anyString())).thenReturn(true);
		when(compilationService.compile(anyString()))
				.thenReturn(new CommandService.CommandResult(0, "BUILD SUCCESSFUL"));

		// Act
		hookController.hookHandler(ctx);

		// Assert
		verify(notificationService).notifyPending(anyString(), eq(mockPayload));
		verify(notificationService).notifySuccess(anyString(), eq(mockPayload)); // Ensures success is reached

		// Ensure ctx.status(200) and ctx.result() are both called
		verify(ctx).status(200);
		verify(ctx).result(contains("Webhook processed. Compilation output:"));
	}
}
