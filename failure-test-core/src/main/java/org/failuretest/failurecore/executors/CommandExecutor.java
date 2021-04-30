package org.failuretest.failurecore.executors;

import org.failuretest.failurecore.CommandExecutionException;

public interface CommandExecutor {
    String executeCommand(String command) throws CommandExecutionException;
}
