package org.failuretest.failurecore.actions;

import org.failuretest.failurecore.Action;
import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.utils.CommandExecutionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkLossAction extends Action {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkLossAction.class);

    private String lossStr;
    private int timeInSec;

    public NetworkLossAction(String lossStr, Integer timeInSec) {
        super(lossStr, timeInSec);
        this.lossStr = lossStr;
        this.timeInSec = timeInSec;
    }

    @Override
    public void init(TestContext testContext) {

    }

    @Override
    public void perform() throws CommandExecutionException {
        LOG.info("network loss on server {}...", getTarget());
        CommandExecutionUtil.logCommandResult(this, getTarget().networkLossFor(lossStr, timeInSec));
    }
}
