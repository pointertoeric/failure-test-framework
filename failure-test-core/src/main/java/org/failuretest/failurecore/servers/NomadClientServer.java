package org.failuretest.failurecore.servers;

import org.failuretest.failurecore.CommandResult;
import org.failuretest.failurecore.annotations.ServiceType;
import org.failuretest.failurecore.utils.CommandExecutionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceType(serviceName = "nomad-client")
public class NomadClientServer extends AbstractEc2Server {
    private static final Logger LOG = LoggerFactory.getLogger(NomadClientServer.class);


    @Override
    public CommandResult restart() {
        LOG.info("restart NomadClientServer node {}", getHost());
        String cmd = String.format("sudo -S reboot");
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
    }
}
