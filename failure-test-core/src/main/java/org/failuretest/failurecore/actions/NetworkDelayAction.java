package org.failuretest.failurecore.actions;

import org.failuretest.failurecore.Action;
import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.utils.CommandExecutionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkDelayAction extends Action {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkDelayAction.class);

    private String delayStr;
    private int timeInSec;

    public NetworkDelayAction(String delayStr, Integer timeInSec) {
        super(delayStr, timeInSec);
        this.delayStr = delayStr;
        this.timeInSec = timeInSec;
    }

    @Override
    public void init(TestContext testContext) {

    }

    @Override
    public void perform() throws CommandExecutionException {
        LOG.info("network delay on server {}...", getTarget());
        CommandExecutionUtil.logCommandResult(this, getTarget().networkDelayFor(delayStr, timeInSec));
    }
}
