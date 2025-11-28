package ecse429;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.*;

public class CategoryTests {

    private static final String BASE = "http://localhost:4567";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final int[] SCALES = {1, 5, 10, 50, 75, 100};

    @FunctionalInterface
    interface CheckedRunnable { void run() throws Exception; }

    private long measure(CheckedRunnable r) throws Exception {
        long start = System.nanoTime();
        r.run();
        return (System.nanoTime() - start) / 1_000_000;  // ms
    }

    private String send(HttpRequest req) throws Exception {
        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private int extractId(String json) {
        return JsonParser.parseString(json)
                .getAsJsonObject()
                .get("id")
                .getAsInt();
    }

    // -----------------------------------------------------------
    // CATEGORY OPERATIONS
    // -----------------------------------------------------------

    /** POST /categories */
    private int createCategory(String title) throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", title);
        obj.addProperty("description", "perf-test");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/categories"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        return extractId(send(req));
    }

    /** PUT /categories/:id */
    private void updateCategory(int id) throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", "updated-category-" + id);
        obj.addProperty("description", "updated-desc");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/categories/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        send(req);
    }

    /** DELETE /categories/:id */
    private void deleteCategory(int id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/categories/" + id))
                .DELETE()
                .build();

        send(req);
    }

    // -----------------------------------------------------------
    // PERFORMANCE TEST
    // -----------------------------------------------------------

    @Test
    public void categoryPerformance() throws Exception {

        PerformanceMonitor monitor = new PerformanceMonitor();

        for (int scale : SCALES) {
            for (int i = 0; i < scale; i++) {

                final int index = i;  // <-- required for lambda usage

                // ---------- ADD ----------
                long addMs = measure(() -> createCategory("cat-" + scale + "-" + index));
                int id = createCategory("cat-perf-" + scale + "-" + index);
                monitor.recordAdd(scale, addMs);

                // ---------- UPDATE ----------
                long updateMs = measure(() -> updateCategory(id));
                monitor.recordUpdate(updateMs);

                // ---------- DELETE ----------
                long deleteMs = measure(() -> deleteCategory(id));
                monitor.recordDelete(deleteMs);
            }
        }

        monitor.writeCSV("category_metrics.csv");
    }
}
