package org.failuretest.failurecore.utils;

import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.CommandResult;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.executors.CommandExecutor;
import org.failuretest.failurecore.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PackageInstaller {
    private static final Logger LOG = LoggerFactory.getLogger(PackageInstaller.class);


    private List<String> packages;
    private CommandExecutor executor;
    private String checkCmd;
    private String installCmd;

    public PackageInstaller(List<String> packages) {
        this.packages = packages;
    }

    public PackageInstaller(CommandExecutor executor, String checkCmd, String installCmd) {
        this.executor = executor;
        this.checkCmd = checkCmd;
        this.installCmd = installCmd;
    }

    public CommandResult install() throws CommandExecutionException {
        LOG.info("install packages with command {}", installCmd);
        if (executor.executeCommand(checkCmd).contains("is not installed")) {
            executor.executeCommand(installCmd);
        } else {
            return new CommandResult();
        }
        // loop to check status
        for (int i = 0; i < 10; i++) {
            if (executor.executeCommand(checkCmd).contains("is not installed")) {
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    throw new IllegalStateException("install package failed");
                }
            } else {
                return new CommandResult();
            }
        }
        throw new IllegalStateException("install package failed" + installCmd);
    }

    public static void installConcurrently(TestContext testContext, List<Server> targets) {
        List<String> packages = testContext.getRequiredPackages();
        if (packages.size() > 0) {
            ExecutorService workerPool = Executors.newFixedThreadPool(targets.size());

            List<Callable<Void>> jobs = targets.stream().map(
                    server -> {
                        return new Callable<Void>() {
                            @Override
                            public Void call() {
                                try {
                                    server.installPackages(packages);
                                } catch (CommandExecutionException e) {
                                    LOG.error("install package failed", e);
                                    throw new IllegalStateException("package install failed");
                                }
                                return null;
                            }
                        };
                    }
            ).collect(Collectors.toList());
            try {
                workerPool.invokeAll(jobs);
            } catch (Exception e) {
                throw new IllegalStateException("install error");
            } finally {
                workerPool.shutdown();
            }
        }
    }
}
