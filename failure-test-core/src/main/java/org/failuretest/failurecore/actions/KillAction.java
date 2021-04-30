package org.failuretest.failurecore.actions;

import org.failuretest.failurecore.Action;
import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.utils.CommandExecutionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KillAction extends Action {
    private static final Logger LOG = LoggerFactory.getLogger(KillAction.class);

    public KillAction() {
        super();
    }

    @Override
    public void init(TestContext testContext) {

    }

    @Override
    public void perform() throws CommandExecutionException {
        LOG.info("killing server {}...", getTarget());
        // store command result
        CommandExecutionUtil.logCommandResult(this, getTarget().kill());
        LOG.info("killed server {}", getTarget());

    }
}
