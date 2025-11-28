package ecse429;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.*;

public class ProjectCategoriesTests {

    private static final String BASE = "http://localhost:4567";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final int[] SCALES = {1, 5, 10, 50, 75, 100};

    @FunctionalInterface interface CheckedRunnable { void run() throws Exception; }

    private long measure(CheckedRunnable r) throws Exception {
        long start = System.nanoTime();
        r.run();
        return (System.nanoTime() - start) / 1_000_000;
    }

    private String send(HttpRequest req) throws Exception {
        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private int extractId(String json) {
        return JsonParser.parseString(json).getAsJsonObject().get("id").getAsInt();
    }

    private int createProject(String title) throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", title);
        obj.addProperty("completed", false);
        obj.addProperty("active", true);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/projects"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        return extractId(send(req));
    }

    private int createCategory(String title) throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", title);
        obj.addProperty("description", "perf");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/categories"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        return extractId(send(req));
    }

    private void linkCategories(int projectId, int categoryId) throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", categoryId);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/projects/" + projectId + "/categories"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        send(req);
    }

    private void unlinkCategories(int projectId, int categoryId) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/projects/" + projectId + "/categories/" + categoryId))
                .DELETE()
                .build();
        send(req);
    }

    private void updateCategory(int categoryId) throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", "updated-category-" + categoryId);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/categories/" + categoryId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        send(req);
    }

    @Test
    public void projectCategoriesPerformance() throws Exception {

        PerformanceMonitor monitor = new PerformanceMonitor();

        for (int scale : SCALES) {
            for (int i = 0; i < scale; i++) {

                int project = createProject("proj-cat-" + scale + "-" + i);
                int category = createCategory("cat-" + scale + "-" + i);

                long addMs = measure(() -> linkCategories(project, category));
                monitor.recordAdd(scale, addMs);

                long updateMs = measure(() -> updateCategory(category));
                monitor.recordUpdate(updateMs);

                long deleteMs = measure(() -> unlinkCategories(project, category));
                monitor.recordDelete(deleteMs);
            }
        }

        monitor.writeCSV("project_categories_metrics.csv");
    }
}
