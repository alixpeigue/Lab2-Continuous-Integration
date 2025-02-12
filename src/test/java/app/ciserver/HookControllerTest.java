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
		when(compilationService.compile(anyString()))
				.thenReturn(new CommandService.CommandResult(1, "FAILURE: Build failed"));

		// Act
		hookController.hookHandler(ctx);

		// Assert
		verify(notificationService).notifyFailure(contains("Compilation failed"), eq(mockPayload));
		verify(ctx).status(500);
	}

	@Test
	void testHookHandler_ExceptionThrown() {
		// Arrange
		when(ctx.bodyAsClass(HookEventModel.class)).thenThrow(new RuntimeException("Unexpected error"));

		// Act
		hookController.hookHandler(ctx);

		// Assert
		verify(ctx).status(500);
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
		verify(ctx).status(500);
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
		when(mockPayload.repository())
				.thenReturn(new RepositoryModel("https://github.com/example/repo.git", "example/repo"));
		when(mockPayload.getBranchName()).thenReturn(Optional.of("main"));
		when(ctx.bodyAsClass(HookEventModel.class)).thenReturn(mockPayload);

		when(gitService.clone(anyString(), anyString())).thenReturn(true);
		when(compilationService.compile(anyString()))
				.thenReturn(new CommandService.CommandResult(0, "BUILD SUCCESSFUL"));

		// Act
		hookController.hookHandler(ctx);

		// Assert
		verify(notificationService).notifyPending(anyString(), eq(mockPayload));
		verify(notificationService).notifySuccess(anyString(), eq(mockPayload));
		verify(ctx).status(200);
	}
}