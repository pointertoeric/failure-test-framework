package org.failuretest.failurecore.actions;

import org.failuretest.failurecore.Action;
import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.utils.CommandExecutionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StressCpuForAction extends Action {
    private static final Logger LOG = LoggerFactory.getLogger(StressCpuForAction.class);

    private int timeInSec;

    public StressCpuForAction(Integer timeInSec) {
        super(timeInSec);
        this.timeInSec = timeInSec;
    }

    @Override
    public void init(TestContext testContext) {

    }

    @Override
    public void perform() throws CommandExecutionException {
        LOG.info("consuming CPU on server {}...", getTarget());
        CommandExecutionUtil.logCommandResult(this, getTarget().stressCpuFor(timeInSec));
    }
}
