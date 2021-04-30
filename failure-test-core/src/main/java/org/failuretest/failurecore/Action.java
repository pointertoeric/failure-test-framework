package org.failuretest.failurecore;

import org.failuretest.failurecore.servers.Server;

/**
 * This class is base call of all actions, to add new action, extends this call
 * and implement {@link #init(TestContext) init()} and {@link #perform() perform()}
 */
public abstract class Action {
    protected TestContext testContext;
    protected Worker actor;
    protected Server target;
    protected Object[] params;

    private boolean needCleanUp = false;

    public Action(Object... params) {
        this.params = params;
    }

    public abstract void init(TestContext testContext);

    public abstract void perform() throws CommandExecutionException;

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Worker getActor() {
        return actor;
    }

    public void setActor(Worker actor) {
        this.actor = actor;
    }

    public Server getTarget() {
        return target;
    }

    public void setTarget(Server target) {
        this.target = target;
    }

    public void setNeedCleanUp(boolean needCleanUp) {
        this.needCleanUp = needCleanUp;
    }

    public boolean isNeedCleanUp() {
        return needCleanUp;
    }
}
