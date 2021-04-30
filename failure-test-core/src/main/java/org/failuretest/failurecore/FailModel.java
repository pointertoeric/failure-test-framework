package org.failuretest.failurecore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * FailModel is the base call of all failure test validation model, implement your own validation model in method
 * {@link #validate})
 */
public abstract class FailModel {
    private static final Logger LOG = LoggerFactory.getLogger(FailModel.class);

    private List<Worker> clients;
    private Partitioner partitioner;
    private PartitionerBuilder partitionerSpec;
    private ClientBuilder clientSpec;
    private ExecutorService wokerPool;
    private TestContext testContext;

    public FailModel(TestContext testContext, ClientBuilder clientSpec, PartitionerBuilder partitionerSpec) {
        this.clients = clientSpec.getClients();
        this.partitioner = partitionerSpec.getPartitioner();
        this.clients.add(this.partitioner);
        this.clientSpec = clientSpec;
        this.partitionerSpec = partitionerSpec;
        this.testContext = testContext;
        this.wokerPool = Executors.newFixedThreadPool(this.clientSpec.getConcurrency() + 1);
        this.testContext.setFailModel(this);
    }

    public List<Worker> getClients() {
        return clients;
    }

    public Partitioner getPartitioner() {
        return partitioner;
    }

    public void setClients(List<Worker> clients) {
        this.clients = clients;
    }

    public void run() {
        //before run, reload server make sure required package installed
        LOG.info("...Loading and initializing Servers...");
        this.testContext.getServerLoader().getServerList();
        LOG.info("...Testing Starting...");
        List<Future<Long>> res;
        try {
            res = wokerPool.invokeAll(clients);
        } catch (Exception e) {
            LOG.error("execution error {}", e);
        } finally {
            wokerPool.shutdown();
        }
    }

    public abstract void validate();
}
