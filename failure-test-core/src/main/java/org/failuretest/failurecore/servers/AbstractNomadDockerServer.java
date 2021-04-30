package org.failuretest.failurecore.servers;

import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.CommandResult;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.annotations.ServiceType;
import org.failuretest.failurecore.utils.CommandExecutionUtil;
import org.failuretest.failurecore.utils.NomadClient;
import org.failuretest.failurecore.utils.PackageInstaller;
import com.hashicorp.nomad.apimodel.Allocation;
import com.hashicorp.nomad.apimodel.MemoryStats;
import com.hashicorp.nomad.apimodel.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

public abstract class AbstractNomadDockerServer extends AbstractServer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNomadDockerServer.class);
    private Allocation allocation;


    public  String getContainerId() throws CommandExecutionException {
        String serviceName = this.getClass().getAnnotation(ServiceType.class).serviceName();
        if (serviceName == null || serviceName.equals("")) {
            throw new IllegalStateException("No serviceName");
        }
        String cmd = String.format("sudo docker ps | grep %s | cut -d \" \" -f 1", serviceName);
        String[] lines = getCommandExecutor().executeCommand(cmd).split("\n");
        return lines[lines.length-1];
    }

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
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), "sudo docker kill " + containerId);
    }

    @Override
    public CommandResult stop() throws CommandExecutionException {
        String containerId = getContainerId();
        LOG.info("stop docker {}", containerId);
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), "sudo docker stop " + containerId);
    }

    @Override
    public CommandResult restart() throws CommandExecutionException {
        String containerId = getContainerId();
        LOG.info("restart docker{}", containerId);
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), "sudo docker restart " + containerId);
    }

    @Override
    public CommandResult start() throws CommandExecutionException {
        String containerId = getContainerId();
        LOG.info("start docker {}", containerId);
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), "sudo docker start " + containerId);
    }

    /**
     * on 4 CPUs system, after stress the CPU, system load looks like
     * load average: 7.64, 7.09, 4.37 (three averages, for 1, 5, and 15 minutes)
     *
     */
    @Override
    public CommandResult stressCpu() throws CommandExecutionException {
        String containerId = getContainerId();
        LOG.info("stress CPU docker {}", containerId);
        // get CPU count
        String cpuCountCmd = "sudo docker exec " + containerId + " nproc";
        String[] lines = getCommandExecutor().executeCommand(cpuCountCmd).trim().split("\n");
        int cpuCount = Integer.parseInt(lines[lines.length-1]);
        for (int i = 0; i < cpuCount; i++) {
            String cmd = "sudo docker exec -d " + containerId + " /bin/bash -c \"while : ; do : ; done &\"";
            getCommandExecutor().executeCommand(cmd);
        }
        return CommandResult.EMPTY_RESULT;
    }

    @Override
    public CommandResult recoverCpu() throws CommandExecutionException {
        String containerId = getContainerId();
        LOG.info("stress CPU docker {}", containerId);
        String cmd = "sudo docker exec -d "
                + containerId
                + " /bin/sh -c \"ps aux | grep \\\"/bin/bash -c while\\\" | cut -d\\\" \\\" -f 8 | xargs kill -9 &\"";
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
    }

    @Override
    public CommandResult stressCpuFor(int timeInSec) throws CommandExecutionException {
        String stressCmd = String.format(
                "v=`nproc`; stress -c \\$v -t %s",
                timeInSec
        );
        String containerId = getContainerId();
        String cmd = "sudo docker exec -d " + containerId + " /bin/bash -c \"" + stressCmd + "&\"";
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
    }


    /**
     * @param percentage: note percentage is percentage of free memory
     * @param timeInSec: after timeInSec stress will recover automatically
     * @throws CommandExecutionException
     */
    @Override
    public CommandResult stressMemoryFor(double percentage, int timeInSec) throws CommandExecutionException {
        TestContext testContext = getTestContext();
        NomadClient nomadClient = new NomadClient(testContext.getConfig().getProperty("nomadHost"));
        Resources resources = nomadClient.getResources(getAllocation());
        MemoryStats resourceUsage = nomadClient.getResourceUsage(getAllocation()).getMemoryStats();
        long cache = resourceUsage.getCache().divide(BigInteger.valueOf(1024*1024)).longValueExact();
        long rss = resourceUsage.getRss().divide(BigInteger.valueOf(1024*1024)).longValueExact();
        // note we have to get memory from nomad instead of linux command
        LOG.info("total memory: {}M, cache: {}M, rss: {}M", resources.getMemoryMb(), cache, rss);
        long freeMemory = resources.getMemoryMb() - cache - rss;
        LOG.info("free memory: {}", freeMemory);
        LOG.info("stress {}% of free memory for {}s", percentage * 100, timeInSec);
        String stressCmd = String.format(
                "stress -m 1 --vm-bytes %sM --vm-keep -t %s",
                (long)(freeMemory * percentage),
                timeInSec
                );
        String containerId = getContainerId();
        String cmd = "sudo docker exec -d " + containerId + " /bin/bash -c \"" + stressCmd + "&\"";
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
        }

    @Override
    public CommandResult installPackages(List<String> packages) throws CommandExecutionException {
        LOG.info("installing packaged on server {} container {}", getHost(), getContainerId());
        String containerId = getContainerId();
        String installCmd = String.format(
                        "sudo docker exec -d %s /bin/sh -c \"apt-get update || apt-get install -y %s\"&",
                        containerId,
                        String.join(" ", packages)
        );
        String checkCmd  = String.format(
                "sudo docker exec -i %s /bin/sh -c \"dpkg -s %s\"",
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