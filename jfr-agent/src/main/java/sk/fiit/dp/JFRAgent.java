package sk.fiit.dp;

import com.google.gson.Gson;
import jdk.jfr.consumer.RecordingStream;
import sk.fiit.dp.model.ExecutionSample;

import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class JFRAgent {

    private static final Gson gson = new Gson();

    private static final class JFREvents {
        private JFREvents() {}
        private static final String EXECUTION_SAMPLE = "jdk.ExecutionSample";
    }

    public static void agentmain(String args) {
        start(args);
    }
    public static void premain(String args) {
        start(args);
    }

    private static void start(String args) {
        System.out.println("[JFR Agent] Starting...");

        int port = 0;
        if (args != null && args.startsWith("port=")) {
            try {
                port = Integer.parseInt(args.substring("port=".length()));
            } catch (Exception ignored) {}
        }
        int finalPort = port;

        Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "JFR-Agent");
            t.setDaemon(true);
            return t;
        }).submit(() -> runAgent(finalPort));
    }

    private static void runAgent(int port) {
        try (
            Socket socket = (port > 0 ? new Socket("127.0.0.1", port) : null);
            PrintWriter out = socket != null
                    ? new PrintWriter(socket.getOutputStream(), true)
                    : new PrintWriter(System.out, true);
            RecordingStream stream = new RecordingStream()
        ) {
            stream.enable(JFREvents.EXECUTION_SAMPLE)
                    .withPeriod(Duration.ofMillis(30))
                    .withStackTrace();

            System.out.println("[JFR Agent] Connected to plugin socket on " + port);

            stream.onEvent(JFREvents.EXECUTION_SAMPLE, event -> {
                var stack = event.getStackTrace();
                if (stack == null) return;

                String threadName = "<unknown>";
                try {
                    var th = event.getThread();
                    if (th != null && th.getJavaName() != null)
                        threadName = th.getJavaName();
                } catch (Throwable ignored) {}


                var frames = new ArrayList<ExecutionSample.Frame>();
                stack.getFrames().forEach(f -> {
                        if (f.isJavaFrame()) {
                            frames.add(new ExecutionSample.Frame(
                                    f.getMethod().getType().getName(),
                                    f.getMethod().getName(),
                                    f.getLineNumber()
                            ));
                        }
                });

                ExecutionSample sample = new ExecutionSample(
                    Instant.now().toEpochMilli(),
                    threadName,
                    frames
                );
                out.println(gson.toJson(sample));
            });

            stream.start();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}