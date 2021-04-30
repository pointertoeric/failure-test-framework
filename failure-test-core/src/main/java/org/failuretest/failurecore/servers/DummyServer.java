package org.failuretest.failurecore.servers;

import org.failuretest.failurecore.executors.CommandExecutor;

public class DummyServer extends AbstractServer {

    @Override
    protected CommandExecutor getCommandExecutor() {
        return null;
    }
}
