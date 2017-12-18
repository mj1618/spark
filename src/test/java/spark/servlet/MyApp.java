package spark.servlet;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

public class MyApp implements SparkApplication {

    public static final String EXTERNAL_FILE = "externalFileServlet.html";

    static File tmpExternalFile;

    public static void main(String args[]){
        new MyApp().init();
    }

    @Override
    public synchronized void init() {
        try {
            externalStaticFileLocation(System.getProperty("java.io.tmpdir"));
            staticFileLocation("/public");

            tmpExternalFile = new File(System.getProperty("java.io.tmpdir"), EXTERNAL_FILE);
            FileWriter writer = new FileWriter(tmpExternalFile);
            writer.write("Content of external file");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        before("/protected/*", (request, response) -> {
            halt(401, "Go Away!");
        });

        get("/hi", (request, response) -> {
            return "Hello World!";
        });

//        get("/:param", (request, response) -> {
//            return "echo: " + request.params(":param");
//        });

        final ExecutorService executorService = Executors.newFixedThreadPool(10);

        get("/async", (request, response) -> {
            CompletableFuture<String> completableFuture
                = new CompletableFuture<>();

            executorService.submit(() -> {
                Thread.sleep(1000);
                completableFuture.complete("Hello async!");
                return null;
            });

            return completableFuture;
        });

        get("/", (request, response) -> {
            return "Hello Root!";
        });

        post("/poster", (request, response) -> {
            String body = request.body();
            response.status(201); // created
            return "Body was: " + body;
        });

        after("/hi", (request, response) -> {
            response.header("after", "foobar");
        });

        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
    }

}
