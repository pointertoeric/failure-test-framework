package org.failuretest.failurecore;

import org.assertj.core.api.SoftAssertions;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is a simple implementation of FailModel, aim to be extended by subclass.
 * Subclass should override method which how to validate request log entry.
 * Also this model provide statistics about the requests and failed request for free.
 */
public abstract class SimpleFailModel extends FailModel {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleFailModel.class);

    public double failRate = 0.0;

    public SimpleFailModel(TestContext testContext, ClientBuilder clientSpec, PartitionerBuilder partitionerSpec) {
        super(testContext, clientSpec, partitionerSpec);
        String failRateStr = testContext.getConfig().getProperty("failRate");
        if (failRateStr != null) {
            setFailRate(Double.parseDouble(failRateStr));
        }
    }

    /**
     * set failRate from config file entry failRate, default is 0.0
     * @param rate failRate double value
     */
    public void setFailRate(double rate) {
        this.failRate = rate;
    }

    /**
     * list of action names treated as client requests
     * @return list of action names treated as client requests
     */
    public abstract List<String> requestActions();

    /**
     * subclass should override this method, how to validate the request from request log entry
     */
    public abstract void assertLogEntry(SoftAssertions softly, LogAction.LogEntry logEntry);

    @Override
    public void validate() {
        LOG.info("validating...");
        SoftAssertions softly = new SoftAssertions();
        final AtomicInteger totalRequests = new AtomicInteger(0);
        long failStartTime = 0;
        long failActionEndTime = 0;

        Map<String, TreeSet<LogAction.LogEntry>> stringTreeSetMap = ModelVerificationHelper.loadLogFile();

        for(Map.Entry<String, TreeSet<LogAction.LogEntry>> entry : stringTreeSetMap.entrySet()) {
            String worker = entry.getKey();
            TreeSet<LogAction.LogEntry> results = entry.getValue();
            LOG.info("worker: {}, results {}", worker, results);
            if (worker.startsWith("client")) {
                for (LogAction.LogEntry logEntry : results) {
                    // exclude wait action
                    if (requestActions().contains(logEntry.getAction())) {
                        totalRequests.getAndIncrement();
                        assertLogEntry(softly, logEntry);
                    }
                }
            } else if (worker.startsWith("partitioner")) {
                for (LogAction.LogEntry logEntry : results) {
                    if (!logEntry.getAction().equalsIgnoreCase("WaitAction")) {
                        failStartTime = logEntry.getStartTime();
                        failActionEndTime = logEntry.getEndTime();
                        break;
                    }
                }
            }
        }
        int error = softly.errorsCollected().size();

        LOG.info("total requests: {}", totalRequests.get());
        LOG.info("total error requests {}", error);
        LOG.info("Fail started at {}", failStartTime);
        LOG.info("Fail action ended at {}", failActionEndTime);

        double failRatio = ((double)error)/totalRequests.get();
        if (failRate != 0 && failRatio <= failRate) {
            assertTrue(true);
        } else {
            softly.assertAll();
        }
    }

}
