package org.failuretest.failurecore.utils;

import org.failuretest.failurecore.Action;
import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.CommandResult;
import org.failuretest.failurecore.executors.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandExecutionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CommandExecutionUtil.class);

    public static CommandResult runPartitionerCommand(CommandExecutor executor, String cmd) {
        try {
            String output = executor.executeCommand(cmd);
            return  CommandResult.withCommandOutput(output);
        } catch (CommandExecutionException e) {
            LOG.error("command error: {}", cmd, e);
            throw new IllegalStateException("run command error");
        }
    }

    public static void logCommandResult(Action action, CommandResult result) {
        action.getActor().getClientContext().setResult(result.getCommandOutput());
    }
}
