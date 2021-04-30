package org.failuretest.failurecore.actions;

import org.failuretest.failurecore.Action;
import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.utils.CommandExecutionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StressMemoryForAction extends Action {
    private static final Logger LOG = LoggerFactory.getLogger(StressMemoryForAction.class);

    private int timeInSec;
    private double percentage;

    public StressMemoryForAction(Double percentage, Integer timeInSec) {
        super(percentage, timeInSec);
        this.timeInSec = timeInSec;
        this.percentage = percentage;
    }

    @Override
    public void init(TestContext testContext) {

    }

    @Override
    public void perform() throws CommandExecutionException {
        LOG.info("consuming Memory on server {}...", getTarget());
        CommandExecutionUtil.logCommandResult(this, getTarget().stressMemoryFor(percentage, timeInSec));
    }
}
