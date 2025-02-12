package app.ciserver;

import io.javalin.http.Context;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TestRunController {

    TestRunPersistenceService testRunPersistenceService;

    public TestRunController(TestRunPersistenceService testRunPersistenceService) {
        this.testRunPersistenceService = testRunPersistenceService;
    }

    public void testRunListHandler(Context ctx) {
        List<TestRunModel> testRuns = testRunPersistenceService.loadAll();
        ctx.render("list.html", Map.of("builds", testRuns));
    }

    public void testRunDetailHandler(Context ctx) {
        Optional<TestRunModel> testRun = testRunPersistenceService.load(ctx.pathParam("commitSHA"));
        if(testRun.isEmpty()) {
            ctx.status(404);
        } else {
            ctx.render("detail.html", Map.of("build", testRun.get()));
        }
    }
}
