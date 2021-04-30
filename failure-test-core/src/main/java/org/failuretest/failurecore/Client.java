package org.failuretest.failurecore;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Client is responsible to send to request or trigger business logic during failures.
 * There maybe multiple clients running concurrently
 * @author  Eric Yang
 */

public class Client extends Worker {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private TestContext testContext;
    private List<Action> actionDefinitions = new ArrayList<>();
    private List<Action> actions = new ArrayList<>();
    private int durationSec;
    private ActionType actionType;
    private LogAction actionLogger = new LogAction();

    public Client(TestContext testContext, String name) {
        super(name);
        this.testContext = testContext;
    }


    public void setActionDefinitions(List<Action> actionDefinitions) {
        this.actionDefinitions = actionDefinitions;
        for (Action actionDefinition : actionDefinitions) {
            try {
                Object[] params = actionDefinition.getParams();
                Class<?>[] types = Arrays.stream(params).map(
                            param -> param.getClass())
                            .collect(Collectors.toList()).toArray(new Class<?>[]{});
                Constructor<?> constructor = actionDefinition.getClass().getDeclaredConstructor(types);
                Action newAction =  (Action) constructor.newInstance(params);
                newAction.setActor(this);
                newAction.init(testContext);
                actions.add(newAction);
            } catch (Exception e) {
                LOG.error("create action error", e);
            }
        }
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public void setDurationSec(int durationSec) {
        this.durationSec = durationSec;
    }

    @Override
    public Long call() {
        if (actionType == ActionType.ONCE) {
            for (Action action : this.actions) {
                try {
                    actionLogger.logAndPerformAction(action);
                } catch (CommandExecutionException e) {
                    LOG.error("Client Action failed, {}", action, e);
                }

            }
        } else {
            long now = System.currentTimeMillis();
            while (true) {
                long current = System.currentTimeMillis();
                if ((current - now) < durationSec * 1000) {
                    for (Action action : this.actions) {
                        try {
                            actionLogger.logAndPerformAction(action);
                        } catch (CommandExecutionException e) {
                            LOG.error("Client Action failed, {}", action, e);
                        }
                    }
                } else return null;
            }
        }
        return null;
    }
}
