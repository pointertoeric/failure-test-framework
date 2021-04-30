package org.failuretest.failurecore;

import org.failuretest.failurecore.serverfilter.ServerFilter;
import org.failuretest.failurecore.servers.Server;

import java.util.ArrayList;
import java.util.List;

public class PartitionerBuilder {

    private TestContext testContext;
    private ActionType actionType;
    private PartitionType partitionType;
    private int duration;
    private Class<? extends Server> targetServer;
    private List<CompositeAction> compositeActions = new ArrayList<>();
    private List<ServerFilter> serverFilters = new ArrayList<>();
    private CompositeAction curAction;
    private Partitioner partitioner;

    public PartitionerBuilder(TestContext testContext) {
        this.testContext = testContext;
    }

    public PartitionerBuilder target(Class<? extends Server> server) {
        CompositeAction cAction = new CompositeAction(testContext, server);
        compositeActions.add(cAction);
        curAction = cAction;
        this.targetServer = server;
        return this;
    }

    public PartitionerBuilder partitionType(PartitionType partitionType) {
        this.partitionType = partitionType;
        return this;
    }

    public PartitionerBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    public PartitionerBuilder actionType(ActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    public PartitionerBuilder serverFilter(ServerFilter serverFilter) {
        this.serverFilters.add(serverFilter);
        return this;
    }

    public PartitionerBuilder action(Action action) {
        curAction.getActionDefinitions().add(action);
        return this;
    }

    public Partitioner getPartitioner() {
        return partitioner;
    }

    public PartitionerBuilder build() {
        partitioner = new Partitioner(testContext, "partitioner");
        partitioner.setActionType(actionType);
        partitioner.setDuration(duration);
        partitioner.setPartitionType(partitionType);
        partitioner.setTarget(targetServer);
        partitioner.setCompositeActions(compositeActions);
        partitioner.setServerFilters(serverFilters);
        return this;
    }
}
