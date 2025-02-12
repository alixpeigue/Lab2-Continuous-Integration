package app.ciserver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.javalin.http.Context;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
	private TestRunPersistenceService testRunPersistenceService;

	@Mock
	private Context ctx;

	@Test
	void SaveSuccessful() {
		HookEventModel mockPayload = mock(HookEventModel.class);
		PusherModel mockPusher = mock(PusherModel.class);

		when(mockPayload.repository())
				.thenReturn(new RepositoryModel("https://github.com/example/repo.git", "example/repo"));
		when(mockPayload.getBranchName()).thenReturn(Optional.of("main"));
		when(mockPayload.after()).thenReturn("abcd1234"); // Mock commit hash
		when(mockPayload.pusher()).thenReturn(mockPusher);
		when(mockPusher.name()).thenReturn("John Doe");
		when(ctx.bodyAsClass(HookEventModel.class)).thenReturn(mockPayload);

		when(gitService.clone(anyString(), anyString())).thenReturn(true);
		when(gitService.checkout(anyString(), eq("abcd1234"))).thenReturn(true); // Use commit hash
		when(compilationService.compile(anyString())).thenReturn(new CommandService.CommandResult(0, "success"));
		when(testRunPersistenceService.save(any())).thenReturn(true);

		hookController.hookHandler(ctx);

		// Capture the argument given to the persistance service.
		ArgumentCaptor<TestRunModel> testRunCaptor = ArgumentCaptor.forClass(TestRunModel.class);

		// Verify that save method is called.
		verify(testRunPersistenceService).save(testRunCaptor.capture());
		TestRunModel capturedTestRun = testRunCaptor.getValue();

		// Check that the values are correct in the captured argument.
		assertEquals("success", capturedTestRun.status());
		assertEquals("abcd1234", capturedTestRun.commitSHA());
		assertEquals("main", capturedTestRun.branchName());
		assertEquals("John Doe", capturedTestRun.pusherName());
		assertEquals("success", capturedTestRun.buildLogs());

	}

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		hookController = new HookController(gitService, compilationService, notificationService,
				testRunPersistenceService);
	}

	@Test
	void testHookHandler_CompilationFailure() {
		// Arrange
		HookEventModel mockPayload = mock(HookEventModel.class);
		PusherModel mockPusher = mock(PusherModel.class);

		when(mockPayload.repository())
				.thenReturn(new RepositoryModel("https://github.com/example/repo.git", "example/repo"));
		when(mockPayload.getBranchName()).thenReturn(Optional.of("main"));
		when(mockPayload.after()).thenReturn("abcd1234"); // Mock commit hash
		when(mockPayload.pusher()).thenReturn(mockPusher);
		when(ctx.bodyAsClass(HookEventModel.class)).thenReturn(mockPayload);

		when(gitService.clone(anyString(), anyString())).thenReturn(true);
		when(gitService.checkout(anyString(), eq("abcd1234"))).thenReturn(true); // Use commit hash
		when(compilationService.compile(anyString()))
				.thenReturn(new CommandService.CommandResult(1, "FAILURE: Build failed"));
		when(testRunPersistenceService.save(any())).thenReturn(true);

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
		when(mockPayload.after()).thenReturn("abcd1234"); // Mock commit hash
		when(ctx.bodyAsClass(HookEventModel.class)).thenReturn(mockPayload);

		when(gitService.clone(anyString(), anyString())).thenReturn(true);
		when(gitService.checkout(anyString(), eq("abcd1234"))).thenReturn(false); // Mock checkout failure

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
		when(mockPayload.after()).thenReturn("abcd1234"); // Mock commit hash
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
		when(mockPayload.after()).thenReturn("abcd1234"); // Mock commit hash
		when(ctx.bodyAsClass(HookEventModel.class)).thenReturn(mockPayload);

		// Act
		hookController.hookHandler(ctx);

		// Assert
		verify(notificationService).notifyFailure(contains("Unsupported ref type"), eq(mockPayload));
	}

	@Test
	void testHookHandler_SuccessfulBuild() {
		// Arrange
		HookEventModel mockPayload = mock(HookEventModel.class);
		RepositoryModel mockRepo = mock(RepositoryModel.class);
		PusherModel mockPusher = mock(PusherModel.class);
		when(mockPayload.repository()).thenReturn(mockRepo);
		when(mockRepo.cloneUrl()).thenReturn("https://github.com/example/repo.git");
		when(mockPayload.getBranchName()).thenReturn(Optional.of("main"));
		when(mockPayload.after()).thenReturn("abcd1234"); // Mock commit hash
		when(ctx.bodyAsClass(HookEventModel.class)).thenReturn(mockPayload);
		when(mockPayload.pusher()).thenReturn(mockPusher);

		// Ensure both clone and checkout succeed
		when(gitService.clone(anyString(), anyString())).thenReturn(true);
		when(gitService.checkout(anyString(), eq("abcd1234"))).thenReturn(true);
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
