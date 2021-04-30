package org.failuretest.failurecore;

import org.failuretest.failurecore.serverfilter.ServerFilter;
import org.failuretest.failurecore.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Partitioner is responsible for injecting failures, there is only one Partitioner in system to make sure we can
 * locate and troubleshooting problem easily.
 */
public class Partitioner extends Worker {
    private static final Logger LOG = LoggerFactory.getLogger(Partitioner.class);

    private TestContext testContext;
    private Class<? extends Server> target;
    private ActionType actionType;
    private PartitionType partitionType;
    private int duration;
    private List<CompositeAction> compositeActions;
    private List<ServerFilter> serverFilters;

    public Partitioner(TestContext testContext, String name) {
        super(name);
        this.testContext = testContext;
    }

    public Class<? extends Server> getTarget() {
        return target;
    }

    public void setTarget(Class<? extends Server> target) {
        this.target = target;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public PartitionType getPartitionType() {
        return partitionType;
    }

    public void setPartitionType(PartitionType partitionType) {
        this.partitionType = partitionType;
    }

    public TestContext getTestContext() {
        return testContext;
    }

    public void setTestContext(TestContext testContext) {
        this.testContext = testContext;
    }

    public List<CompositeAction> getCompositeActions() {
        return compositeActions;
    }

    public void setCompositeActions(List<CompositeAction> compositeActions) {
        for (CompositeAction compositeAction : compositeActions) {
            compositeAction.init(this);
            compositeAction.setPartitionType(partitionType);
        }
        this.compositeActions = compositeActions;
    }

    public void setServerFilters(List<ServerFilter> serverFilters) {
        this.serverFilters = serverFilters;
    }

    public List<ServerFilter> getServerFilters() {
        return serverFilters;
    }

    @Override
    public Long call() {
        Instant beginTime = Instant.now();
        if (actionType == ActionType.ONCE) {
            for (CompositeAction action : this.compositeActions) {
                action.run();
            }
        } else { // REPEAT
            while (true) {
                Instant currentTime = Instant.now();
                if (Duration.between(beginTime, currentTime).toMillis() < duration * 1000) {
                    for (CompositeAction action : this.compositeActions) {
                        action.run();
                    }
                } else return null;
            }
        }
        return null;
    }
}
