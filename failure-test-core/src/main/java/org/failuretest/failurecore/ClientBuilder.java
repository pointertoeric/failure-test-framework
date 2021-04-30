package org.failuretest.failurecore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * ClientBuilder is used to build Client, like actions to be executed, duration, action type .etc
 */
public class ClientBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ClientBuilder.class);

    private List<Action> actions = new ArrayList<>();
    private int durationSec;
    private ActionType actionType;
    private int concurrency;
    private TestContext testContext;
    private List<Worker> clients = new ArrayList<>();

    public ClientBuilder(TestContext testContext) {
        this.durationSec = 1;
        this.actionType = ActionType.ONCE;
        this.concurrency = 1;
        this.testContext = testContext;
    }

    public ClientBuilder action(Action action) {
        this.actions.add(action);
        return this;
    }

    public ClientBuilder duration(int duration) {
        this.durationSec = duration;
        return this;
    }

    public ClientBuilder actionType(ActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    public ClientBuilder concurrency(int concurrency) {
        this.concurrency = concurrency;
        return this;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public List<Action> getActions() {
        return actions;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public List<Worker> getClients() {
        return clients;
    }

    public ClientBuilder build() {
        for (int i = 0; i < concurrency; i++) {
            try {
                Client client = new Client(testContext, "client-" + i);
                client.setActionDefinitions(actions);
                client.setActionType(actionType);
                client.setDurationSec(durationSec);
                clients.add(client);
            } catch (Exception e) {
                LOG.error("create client error", e);
            }

        }
        return this;
    }
}
