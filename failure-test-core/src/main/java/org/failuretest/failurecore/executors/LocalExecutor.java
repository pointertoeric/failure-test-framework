package org.failuretest.failurecore.executors;

import org.failuretest.failurecore.CommandExecutionException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalExecutor implements CommandExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(LocalExecutor.class);

    public LocalExecutor() {
    }

    @Override
    public String executeCommand(String command) throws CommandExecutionException {
        try {
            Process p = Runtime.getRuntime().exec(command);
            return IOUtils.toString(p.getInputStream(), "UTF-8");
        } catch (Exception e) {
            LOG.error("local command error: {}", command, e);
            throw new CommandExecutionException("local command error");
        }

    }
}
