package app.ciserver;

import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TestRunControllerTest {

    @Mock
    TestRunPersistenceService testRunPersistenceService;

    @Mock
    Context context;

    @InjectMocks
    TestRunController testRunController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListHandler() {
        List<TestRunModel> testRuns = List.of(
                new TestRunModel(null, "success", "COMMITHASH1", "main", "pusher", "Build success"),
                new TestRunModel(null, "failure", "COMMITHASH2", "main", "pusher", "Build failure")
        );
        when(testRunPersistenceService.loadAll()).thenReturn(testRuns);
        testRunController.testRunListHandler(context);
        verify(context).render("list.html", Map.of("builds", testRuns));
    }

    @Test
    void testDetailHandlerExist() {
        String hash = "COMMITHASH";
        TestRunModel testRun = new TestRunModel(null, "success", hash, "main", "pusher", "Build success");
        when(testRunPersistenceService.load(anyString())).thenReturn(Optional.of(testRun));
        when(context.pathParam(anyString())).thenReturn(hash);

        testRunController.testRunDetailHandler(context);

        verify(context).pathParam("commitSHA");
        verify(context).render("detail.html", Map.of("build", testRun));
    }

    @Test
    void testDetailHandlerDoesNotExist() {
        String hash = "COMMITHASH";
        when(testRunPersistenceService.load(anyString())).thenReturn(Optional.empty());
        when(context.pathParam(anyString())).thenReturn(hash);

        testRunController.testRunDetailHandler(context);

        verify(context).pathParam("commitSHA");
        verify(context).status(404);
    }

}