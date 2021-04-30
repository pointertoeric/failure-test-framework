package org.failuretest.failurecore.servers;

import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.CommandResult;
import org.failuretest.failurecore.utils.CommandExecutionUtil;
import org.failuretest.failurecore.utils.PackageInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractEc2Server extends AbstractServer{
    private static final Logger LOG = LoggerFactory.getLogger(AbstractEc2Server.class);

    @Override
    public CommandResult restart() {
        LOG.info("restart EC2 node {}", getHost());
        String cmd = String.format("sudo -S service %s restart", getServiceName());
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
    }

    @Override
    public CommandResult start() {
        LOG.info("start EC2 node {}", getHost());
        String cmd = String.format("sudo -S service %s start", getServiceName());
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
    }

    @Override
    public CommandResult stop() {
        LOG.info("stop EC2 node {}", getHost());
        String cmd = String.format("sudo -S service %s stop", getServiceName());
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
    }

    @Override
    public CommandResult kill() {
        LOG.info("kill EC2 node {}", getHost());
        String pidCmd = String.format("ids=`ps aux | grep %s | tr -s \" \" |cut -d \" \" -f 2`;", getServiceName());
        String killCmd = "for i in $ids; do  sudo -S kill -9  $i; done";
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), pidCmd + killCmd);
    }

    @Override
    public CommandResult stressCpuFor(int timeInSec) {
        String stressCmd = String.format(
                "v=`nproc`; stress -c $v -t %s",
                timeInSec
        );
        String cmd = stressCmd + "&";
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
    }

    /**
     * @param percentage: note percentage is percentage of free memory
     * @param timeInSec: after timeInSec stress will recover automatically
     * @throws CommandExecutionException
     */
    @Override
    public CommandResult stressMemoryFor(double percentage, int timeInSec) {
        LOG.info("using {} free memory for {}s", percentage, timeInSec);
        // allow two digits float, e.g. 1.02, 0.88
        int numerator = (int)(percentage*1000);
        String freeMemoryCmd = "free=`cat /proc/meminfo | grep MemAvailable | tr -s \" \" | cut -d \" \" -f 2`;";
        String stressCmd = String.format(
                "let a=$free*%d;let b=$a/1000; stress -m 1 --vm-bytes ${b}K --vm-keep -t %s",
                numerator,
                timeInSec
        );
        String cmd = freeMemoryCmd + stressCmd + "&";
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
    }

    @Override
    public CommandResult installPackages(List<String> packages) throws CommandExecutionException {
        LOG.info("installing packaged on server {}", getHost());
        String installCmd = String.format(
                "sudo -S apt-get update || sudo -S apt-get install -y %s &",
                String.join(" ", packages)
        );
        String checkCmd = String.format(
                "dpkg -s %s",
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
