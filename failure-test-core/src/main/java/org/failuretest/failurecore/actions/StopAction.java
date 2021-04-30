package org.failuretest.failurecore.actions;

import org.failuretest.failurecore.Action;
import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.utils.CommandExecutionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopAction extends Action {
    private static final Logger LOG = LoggerFactory.getLogger(StopAction.class);

    public StopAction() {
        super();
    }

    @Override
    public void init(TestContext testContext) {

    }

    @Override
    public void perform() throws CommandExecutionException {
        LOG.info("Stopping server {}...", getTarget());
        CommandExecutionUtil.logCommandResult(this, getTarget().stop());
        LOG.info("stopped server {}", getTarget());

    }
}
