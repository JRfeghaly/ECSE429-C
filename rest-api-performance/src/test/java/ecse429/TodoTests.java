package ecse429;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.*;

public class TodoTests {

    private static final String BASE = "http://localhost:4567";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final int[] SCALES = {1, 5, 10, 50, 75, 100};

    @FunctionalInterface
    interface CheckedRunnable { void run() throws Exception; }

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

    /** POST /todos */
    private int createTodo(String title) throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", title);
        obj.addProperty("doneStatus", false);
        obj.addProperty("description", "perf-test");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        return extractId(send(req));
    }

    /** PUT /todos/:id */
    private void updateTodo(int id) throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", "updated-todo-" + id);
        obj.addProperty("doneStatus", true);
        obj.addProperty("description", "updated-desc");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/todos/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        send(req);
    }

    /** DELETE /todos/:id */
    private void deleteTodo(int id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/todos/" + id))
                .DELETE()
                .build();

        send(req);
    }

    @Test
    public void todoPerformance() throws Exception {

        PerformanceMonitor monitor = new PerformanceMonitor();

        for (int scale : SCALES) {
            for (int i = 0; i < scale; i++) {

                final int index = i;   // <-- FIXED HERE

                // ---------- ADD ----------
                long addMs = measure(() -> createTodo("todo-" + scale + "-" + index));
                int id = createTodo("todo-perf-" + scale + "-" + index);
                monitor.recordAdd(scale, addMs);

                // ---------- UPDATE ----------
                long updateMs = measure(() -> updateTodo(id));
                monitor.recordUpdate(updateMs);

                // ---------- DELETE ----------
                long deleteMs = measure(() -> deleteTodo(id));
                monitor.recordDelete(deleteMs);
            }
        }

        monitor.writeCSV("todo_metrics.csv");
    }
}
