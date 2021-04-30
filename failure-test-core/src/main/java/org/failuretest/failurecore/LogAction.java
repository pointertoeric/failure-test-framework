package org.failuretest.failurecore;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execution result of actions will be logged in log file during testing, so can be used for FailModel
 * for validating.
 */
public class LogAction {
    private static final Logger LOG = LoggerFactory.getLogger(LogAction.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public void logAndPerformAction(Action action) throws CommandExecutionException {
        long startTime = System.currentTimeMillis();
        action.perform();
        long endTime = System.currentTimeMillis();
        Worker worker = action.getActor();
        String workerName = worker.getName();
        LogEntry logEntry = new LogEntry();
        logEntry.setStartTime(startTime);
        logEntry.setEndTime(endTime);
        logEntry.setAction(action.getClass().getSimpleName());
        logEntry.setWorkerName(workerName);
        logEntry.setResult(worker.getClientContext().getResult());
        if (action.getActor().getClass() == Partitioner.class) {
            logEntry.setTarget(action.getTarget().getHost());
        }
        logEntry.setParams(action.getParams());

        try {
            LOG.info("{}", OBJECT_MAPPER.writeValueAsString(logEntry));
        } catch (Exception e) {
            LOG.error("Error converting log entry: {}", logEntry, e);
        }
    }

    public static class LogEntry {
        private long startTime;
        private long endTime;
        private String workerName;
        private String action;
        private Object result;
        private String target;
        private Object[] params;

        public LogEntry() {
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public String getWorkerName() {
            return workerName;
        }

        public void setWorkerName(String workerName) {
            this.workerName = workerName;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Object[] getParams() {
            return params;
        }

        public void setParams(Object[] params) {
            this.params = params;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder
                    .append("|workerName: " + workerName)
                    .append("|action: " + action)
                    .append("|startTime: " + startTime)
                    .append("|endTime: " + endTime);
            return builder.toString();
        }
    }
}
