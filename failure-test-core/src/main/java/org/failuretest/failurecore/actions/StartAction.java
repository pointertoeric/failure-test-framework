package org.failuretest.failurecore.actions;

import org.failuretest.failurecore.Action;
import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.utils.CommandExecutionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartAction extends Action {
    private static final Logger LOG = LoggerFactory.getLogger(StartAction.class);

    public StartAction() {
        super();
        setNeedCleanUp(true);
    }

    @Override
    public void init(TestContext testContext) {

    }

    @Override
    public void perform() throws CommandExecutionException {
        LOG.info("starting server {}...", getTarget());
        CommandExecutionUtil.logCommandResult(this, getTarget().start());
        LOG.info("started server {}", getTarget());
    }
}
