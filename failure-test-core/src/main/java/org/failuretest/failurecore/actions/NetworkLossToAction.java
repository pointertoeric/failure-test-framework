package org.failuretest.failurecore.actions;

import org.failuretest.failurecore.Action;
import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.utils.CommandExecutionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkLossToAction extends Action {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkLossToAction.class);

    private String lossStr;
    private int timeInSec;
    private String[] ipList;

    public NetworkLossToAction(String lossStr, Integer timeInSec, String[] ipList) {
        super(lossStr, timeInSec, ipList);
        this.lossStr = lossStr;
        this.timeInSec = timeInSec;
        this.ipList = ipList;
    }

    @Override
    public void init(TestContext testContext) {

    }

    @Override
    public void perform() throws CommandExecutionException {
        LOG.info("network delay on server {}...", getTarget());
        CommandExecutionUtil.logCommandResult(this, getTarget().networkLossTo(lossStr, timeInSec, ipList));
    }
}
