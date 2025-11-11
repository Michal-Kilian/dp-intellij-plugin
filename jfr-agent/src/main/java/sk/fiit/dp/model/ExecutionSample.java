package sk.fiit.dp.model;

import java.util.List;

public class ExecutionSample {
    public long timestamp;
    public String threadName;
    public List<Frame> frames;

    public ExecutionSample(long timestamp, String threadName, List<Frame> frames) {
        this.timestamp = timestamp;
        this.threadName = threadName;
        this.frames = frames;
    }

    public static class Frame {
        public String className;
        public String method;
        public int line;

        public Frame(String className, String method, int line) {
            this.className = className;
            this.method = method;
            this.line = line;
        }
    }
}
