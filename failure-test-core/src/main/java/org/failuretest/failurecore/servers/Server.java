package org.failuretest.failurecore.servers;

import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.CommandResult;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.executors.CommandExecutor;

import java.util.List;

/**
 * Server is base interface of all server implementation.
 * @author  Eric Yang
 */

public interface Server {

    TestContext getTestContext();

    void setTestContext(TestContext testContext);

    String getPassword();

    void setPassword(String password);

    String getUsername();

    void setUsername(String username);

    String getHost();

    void setHost(String host);

    String getBastionHost();

    void setBastionHost(String bastionHost);

    int getPort();

    void setPort(int port);

    String getServiceName();

    void setServiceName(String serviceName);

    String getSshKeyFile();

    void setSshKeyFile(String sshKeyFile);

    String getSshUser();

    void setSshUser(String sshUser);

    CommandResult kill() throws CommandExecutionException;

    CommandResult start() throws CommandExecutionException;

    CommandResult restart() throws CommandExecutionException;

    CommandResult stop() throws CommandExecutionException;

    CommandResult stressCpu() throws CommandExecutionException;

    CommandResult recoverCpu() throws CommandExecutionException;

    CommandResult stressMemory() throws CommandExecutionException;

    CommandResult recoverMemory() throws CommandExecutionException;

    CommandResult networkDelayFor(String delay, int timeInSec) throws CommandExecutionException;

    CommandResult networkDelayTo(String delayStr, int timeInSec, String[] ipList) throws CommandExecutionException;

    CommandResult networkLossTo(String lossStr, int timeInSec, String[] ipList) throws CommandExecutionException;

    CommandResult networkLossFor(String loss, int timeInSec) throws CommandExecutionException;

    CommandResult networkLoss(String lossPercentage) throws CommandExecutionException;

    CommandResult recoverNetwork() throws CommandExecutionException;

    CommandResult stressCpuFor(int timeInSec) throws CommandExecutionException;

    CommandResult stressMemoryFor(double percentage, int timeInSec) throws CommandExecutionException;

    CommandResult installPackages(List<String>  packages) throws CommandExecutionException;

    void setMetaData(String key, Object value);

    Object getMetaData(String key);

    Class<? extends CommandExecutor> getExecutorClass();

    void setExecutorClass(Class<? extends CommandExecutor> executorClass);

    default void init() {}

}
