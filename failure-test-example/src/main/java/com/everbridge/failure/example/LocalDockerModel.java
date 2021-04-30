package org.failuretest.failure.example;

import org.failuretest.failurecore.*;
import org.assertj.core.api.SoftAssertions;
import org.failuretest.failurecore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalDockerModel extends FailModel {
    private static final Logger LOG = LoggerFactory.getLogger(LocalDockerModel.class);

    public LocalDockerModel(TestContext testContext, ClientBuilder clientBuilder, PartitionerBuilder partitionerBuilder) {
        super(testContext, clientBuilder, partitionerBuilder);
    }

    @Override
    public void validate() {
        LOG.info("validating...");
        SoftAssertions softly = new SoftAssertions();
        final AtomicInteger totalRequests = new AtomicInteger(0);

        Map<String, TreeSet<LogAction.LogEntry>> stringTreeSetMap = ModelVerificationHelper.loadLogFile();

        for(Map.Entry<String, TreeSet<LogAction.LogEntry>> entry : stringTreeSetMap.entrySet()) {
            String worker = entry.getKey();
            TreeSet<LogAction.LogEntry> results = entry.getValue();
            LOG.info("worker: {}, results {}", worker, results);
            if (worker.startsWith("client")) {
                for (LogAction.LogEntry logEntry : results) {
                    // exclude wait action
                    if (logEntry.getAction().equalsIgnoreCase("WriteAction")) {
                        totalRequests.getAndIncrement();
                        String id = "";
                        try {
                            id = (String) ((Map<String, Object>)logEntry.getResult()).get("id");
                            LOG.info("id {}", id);
                            softly.assertThat(id)
                                    .isNotEmpty();
                        } catch (Exception e) {
                            softly.assertThat(id)
                                    .isNotEmpty();
                        }
                    }
                }
            }
        }
        int error = softly.errorsCollected().size();

        LOG.info("total requests: {}", totalRequests.get());
        LOG.info("total error requests {}", error);

        softly.assertAll();
    }

}
