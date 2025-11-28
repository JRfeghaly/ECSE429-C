package ecse429;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.*;

public class ProjectTests {

    private static final String BASE = "http://localhost:4567";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final int[] SCALES = {1, 5, 10, 50, 75, 100};

    @FunctionalInterface 
    interface CheckedRunnable { 
        void run() throws Exception; 
    }

    private long measure(CheckedRunnable r) throws Exception {
        long start = System.nanoTime();
        r.run();
        return (System.nanoTime() - start) / 1_000_000; // ms
    }

    private String send(HttpRequest req) throws Exception {
        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private int extractId(String json) {
        return JsonParser.parseString(json)
                .getAsJsonObject()
                .get("id").getAsInt();
    }

    // -----------------------------------------------------------
    // PROJECT OPERATIONS
    // -----------------------------------------------------------

    /** POST /projects */
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

    /** PUT /projects/:id */
    private void updateProject(int id) throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", "updated-project-" + id);
        obj.addProperty("completed", true);
        obj.addProperty("active", false);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/projects/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        send(req);
    }

    /** DELETE /projects/:id */
    private void deleteProject(int id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/projects/" + id))
                .DELETE()
                .build();

        send(req);
    }

    // -----------------------------------------------------------
    // PERFORMANCE TEST
    // -----------------------------------------------------------

    @Test
    public void projectPerformance() throws Exception {

        PerformanceMonitor monitor = new PerformanceMonitor();

        for (int scale : SCALES) {
            for (int i = 0; i < scale; i++) {
                final int index = i; // Declare a final variable to use in the lambda

                // -------- ADD --------
                long addMs = measure(() -> createProject("proj-" + scale + "-" + index));
                int id = createProject("proj-perf-" + scale + "-" + index);
                monitor.recordAdd(scale, addMs);

                // -------- UPDATE --------
                long updateMs = measure(() -> updateProject(id));
                monitor.recordUpdate(updateMs);

                // -------- DELETE --------
                long deleteMs = measure(() -> deleteProject(id));
                monitor.recordDelete(deleteMs);
            }
        }

        monitor.writeCSV("project_metrics.csv");
    }
}
