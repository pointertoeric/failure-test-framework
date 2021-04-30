package org.failuretest.failurecore.servers;

import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.CommandResult;
import org.failuretest.failurecore.utils.CommandExecutionUtil;
import org.failuretest.failurecore.utils.PackageInstaller;
import com.hashicorp.nomad.apimodel.Allocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractDockerServer extends AbstractServer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDockerServer.class);
    private Allocation allocation;


    protected abstract String getContainerId() throws CommandExecutionException;

    public Allocation getAllocation() {
        return allocation;
    }

    public void setAllocation(Allocation allocation) {
        this.allocation = allocation;
    }

    @Override
    public CommandResult kill() throws CommandExecutionException {
        String containerId = getContainerId();
        LOG.info("kill docker {}", containerId);
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), "docker kill " + containerId);
    }

    @Override
    public CommandResult stop() throws CommandExecutionException {
        String containerId = getContainerId();
        LOG.info("stop docker {}", containerId);
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), "docker stop " + containerId);
    }

    @Override
    public CommandResult restart() throws CommandExecutionException {
        String containerId = getContainerId();
        LOG.info("restart docker {}", containerId);
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), "docker restart " + containerId);
    }

    @Override
    public CommandResult start() throws CommandExecutionException {
        String containerId = getContainerId();
        LOG.info("start docker {}", containerId);
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), "docker start " + containerId);
    }

    @Override
    public CommandResult stressCpu() throws CommandExecutionException {
        String containerId = getContainerId();
        LOG.info("stress CPU docker {}", containerId);
        // get CPU count
        String cpuCountCmd = "docker exec " + containerId + " nproc";
        String[] lines = getCommandExecutor().executeCommand(cpuCountCmd).trim().split("\n");
        int cpuCount = Integer.parseInt(lines[lines.length-1]);
        for (int i = 0; i < cpuCount; i++) {
            String cmd = "docker exec -d " + containerId + " /bin/bash -c \"while : ; do : ; done &\"";
            getCommandExecutor().executeCommand(cmd);
        }
        return CommandResult.EMPTY_RESULT;
    }

    @Override
    public CommandResult recoverCpu() throws CommandExecutionException {
        String containerId = getContainerId();
        LOG.info("stress CPU docker {}", containerId);
        String cmd = "docker exec -d "
                + containerId
                + " /bin/sh -c \"ps aux | grep \\\"/bin/bash -c while\\\" |  tr -s \\\" \\\" | cut -d\\\" \\\" -f 8 | xargs kill -9 &\"";
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
    }

    @Override
    public CommandResult stressCpuFor(int timeInSec) throws CommandExecutionException {
        String stressCmd = String.format(
                "v=`nproc`; stress -c \\$v -t %s",
                timeInSec
        );
        String containerId = getContainerId();
        String cmd = "docker exec -d " + containerId + " /bin/bash -c \"" + stressCmd + "&\"";
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
    }

    @Override
    public CommandResult installPackages(List<String> packages) throws CommandExecutionException {
        LOG.info("installing packaged on server {} container {}", getHost(), getContainerId());
        String containerId = getContainerId();
        String installCmd = String.format(
                        "docker exec -d %s /bin/sh -c \"apt-get update || apt-get install -y %s\"&",
                        containerId,
                        String.join(" ", packages)
        );
        String checkCmd  = String.format(
                "docker exec -i %s /bin/sh -c \"dpkg -s %s\"",
                containerId,
                String.join(" ", packages)
        );
        // wait for installed
        if (packages.isEmpty()) {
            return CommandResult.EMPTY_RESULT;
        }
        PackageInstaller installer = new PackageInstaller(getCommandExecutor(), checkCmd, installCmd);
        return installer.install();
    }
}