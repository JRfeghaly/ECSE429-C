package ecse429;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.*;

public class CategoryTodosTests {

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

    private int createTodo(String title) throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", title);
        obj.addProperty("doneStatus", false);
        obj.addProperty("description", "perf");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        return extractId(send(req));
    }

    private void linkTodos(int categoryId, int todoId) throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", todoId);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/categories/" + categoryId + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        send(req);
    }

    private void unlinkTodos(int categoryId, int todoId) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/categories/" + categoryId + "/todos/" + todoId))
                .DELETE()
                .build();
        send(req);
    }

    private void updateTodo(int todoId) throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("title", "updated-todo-" + todoId);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/todos/" + todoId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        send(req);
    }

    @Test
    public void categoryTodosPerformance() throws Exception {

        PerformanceMonitor monitor = new PerformanceMonitor();

        for (int scale : SCALES) {
            for (int i = 0; i < scale; i++) {

                int category = createCategory("cat-todo-" + scale + "-" + i);
                int todo = createTodo("todo-" + scale + "-" + i);

                long addMs = measure(() -> linkTodos(category, todo));
                monitor.recordAdd(scale, addMs);

                long updateMs = measure(() -> updateTodo(todo));
                monitor.recordUpdate(updateMs);

                long deleteMs = measure(() -> unlinkTodos(category, todo));
                monitor.recordDelete(deleteMs);
            }
        }

        monitor.writeCSV("category_todos_metrics.csv");
    }
}
