package org.failuretest.failurecore;

import org.failuretest.failurecore.servers.Server;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CompositeAction has a list of actions to be run, all actions in CompositeAction will target same server node.
 */
public class CompositeAction {
    private static final Logger LOG = LoggerFactory.getLogger(CompositeAction.class);

    private Class<? extends Server> target;
    private List<Action> actionDefinitions = new ArrayList<>();
    private PartitionType partitionType;
    private List<Action> actions = new ArrayList<>();
    private TestContext testContext;
    private LogAction actionLogger;

    public CompositeAction(TestContext testContext, Class<? extends Server> target) {
        this.target = target;
        this.testContext = testContext;
        this.actionLogger = new LogAction();
    }

    public List<Action> getActionDefinitions() {
        return actionDefinitions;
    }

    public void setActionDefinitions(List<Action> actionDefinitions) {
        this.actionDefinitions = actionDefinitions;
    }

    public Class<? extends Server> getTarget() {
        return target;
    }

    public void setTarget(Class<? extends Server> target) {
        this.target = target;
    }

    public PartitionType getPartitionType() {
        return partitionType;
    }

    public void setPartitionType(PartitionType partitionType) {
        this.partitionType = partitionType;
    }

    public void init(Partitioner actor) {
        // generate Action by ActionDefinition
        for (Action actionDefinition : actionDefinitions) {
            try {
                Object[] params = actionDefinition.getParams();
                Class<?>[] types = (Class<?>[]) Arrays.stream(params).map(
                        param -> param.getClass())
                        .collect(Collectors.toList()).toArray(new Class<?>[]{});
                Constructor<?> constructor = actionDefinition.getClass().getDeclaredConstructor(types);
                Action newAction =  (Action) constructor.newInstance(params);
                newAction.setActor(actor);
                newAction.init(testContext);
                actions.add(newAction);
            } catch (Exception e) {
                LOG.error("create action error {}", e);
            }
        }
    }

    public void run() {
        // for composite action we have to select new target server
        List<Server> curServers = selectNodes();
        for (Server server : curServers) {
            LOG.info("run actions for server {} at current time: {}", server, System.currentTimeMillis());
            for (Action action : actions) {
                action.setTarget(server);
                try {
                    this.actionLogger.logAndPerformAction(action);
                } catch (CommandExecutionException e) {
                    LOG.error("execute action error {}", action, e);
                    testContext.addFailedAction(action);
                    throw new IllegalStateException(e.getMessage());
                }
            }
        }
    }

    private List<Server> selectNodes() {
        String serviceLookupName = ServerRegistry.getServiceNameByClass(target);
        List<Server> servers = testContext.getServerLoader().getServerListByServiceName(serviceLookupName);
        // run ServerFilters
        List<Server> filterServers = servers.stream().filter(
                server ->
                        testContext.getFailModel().getPartitioner().getServerFilters().stream().allMatch(
                                serverFilter -> serverFilter.test(server)))
                .collect(Collectors.toList());
        if (partitionType == PartitionType.RANDOM) {
            return Lists.newArrayList(filterServers.get(new Random().nextInt(filterServers.size())));
        } else if (partitionType == PartitionType.ALL) {
            return filterServers;

        } else if (partitionType == PartitionType.MAJORITY) {
            Collections.shuffle(filterServers);
            // return 2/3 servers, make sure there are >= 3 servers, otherwise all servers will be returned.
            int lastIndex = (int)Math.ceil(filterServers.size() * 0.6);
            return  filterServers.subList(0, lastIndex);
        }
        else {
            if (filterServers.size() > 0) {
                return Lists.newArrayList(filterServers.get(0));
            } else {
                throw new IllegalStateException("server list is empty");
            }
        }
    }
}
