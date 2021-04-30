package org.failuretest.failurecore.servers;

import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.CommandResult;
import org.failuretest.failurecore.executors.*;
import org.failuretest.failurecore.ServerMetaData;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.trafficcontrol.NetEmOperator;
import org.failuretest.failurecore.utils.CommandExecutionUtil;
import org.failuretest.failurecore.utils.CommandGenerator;
import org.failuretest.failurecore.utils.TemplateEngine;
import org.failuretest.failurecore.executors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * AbstractServer is base class of all server implementation.
 */
public abstract class AbstractServer implements Server {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractServer.class);

    private static final String IPADDRESS_PATTERN =
                    "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private static final Pattern IP_PATTERN = Pattern.compile(IPADDRESS_PATTERN);

    private String host;
    private int port;
    private String username;
    private String password;
    private String serviceName;
    private String aliasName;
    private String sshKeyFile;
    private String sshUser;
    private String bastionHost;
    private TestContext testContext;
    private ServerMetaData serverMetaData = new ServerMetaData();
    private Class<? extends CommandExecutor> executorClass;

    protected CommandExecutor getCommandExecutor() {
        if (executorClass == LocalExecutor.class) {
            return new LocalExecutor();
        } else if (executorClass == SshPasswordExecutor.class) {
            return new SshPasswordExecutor(getHost(), getUsername(), getPassword());
        } else if(executorClass == SshBastionExecutor.class) {
            return new SshBastionExecutor(getBastionHost(), getHost(), getSshKeyFile(), getSshUser());
        } else if(executorClass == SshExecutor.class) {
            return new SshExecutor(getHost(), getSshKeyFile(), getSshUser());
        }
        return null;
    }

    public void init() {
    }

    public void setTestContext(TestContext testContext) {
        this.testContext = testContext;
    }

    public TestContext getTestContext() {
        return testContext;
    }

    protected String interfaceCmd(String host) {
        return String.format(
                "v=`ip addr | grep %s | tr -s \" \" |cut -d \" \" -f 8`;",
                host);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSshKeyFile() {
        return sshKeyFile;
    }

    public void setSshKeyFile(String sshKeyFile) {
        this.sshKeyFile = sshKeyFile;
    }

    public String getSshUser() {
        return sshUser;
    }

    public void setSshUser(String sshUser) {
        this.sshUser = sshUser;
    }

    @Override
    public CommandResult kill() throws CommandExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CommandResult restart() throws CommandExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CommandResult stop() throws CommandExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CommandResult start() throws CommandExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CommandResult stressCpu() throws CommandExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CommandResult recoverCpu() throws CommandExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CommandResult stressMemory() throws CommandExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CommandResult recoverMemory() throws CommandExecutionException {
        throw new UnsupportedOperationException();
    }

    private String getPublicIp(String host) throws CommandExecutionException {
        if (IP_PATTERN.matcher(host).matches()) {
            return host;
        } else {
            try {
                return InetAddress.getByName(host).toString();
            } catch (UnknownHostException e) {
                throw new CommandExecutionException("can not resolve host: " + host);
            }
        }
    }

    private String getPrivateIp(String host) throws CommandExecutionException {
        LOG.info("get private ip for {}", host);
        if (IP_PATTERN.matcher(host).matches()) {
            return host;
        } else {
            try {
                String[] lines = getCommandExecutor().executeCommand("hostname -I").split("\n");
                LOG.info("private Ip is {}", lines[lines.length - 1]);
                return lines[lines.length - 1];
            } catch (Exception e) {
                throw new CommandExecutionException("cannot get private Ip");
            }
        }
    }

    private String buildNetworkCmd(String operator, String param, int timeInsec) {
        Map<String, Object> data = new HashMap<>();
        data.put("operator", operator);
        data.put("param", param);
        data.put("timeInSec", timeInsec);
        String script = TemplateEngine.loadTemplate("scripts/network_script.sh", data);
        return CommandGenerator.generateCommand(script);
    }

    /**
     * currently inject network delay on ec2 host, to inject in containter,
     * have to enable network CAP for docker container
     * @param delay: e.g. 100ms, 1s
     */
    @Override
    public CommandResult networkDelayFor(String delay, int timeInsec) throws CommandExecutionException {
        String host = getPrivateIp(getHost());
        LOG.info("network delay {} on {}", delay, host);
        String cmd = buildNetworkCmd("delay", delay, timeInsec);
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
    }

    public String buildNetworkToCommand(NetEmOperator operator, String param, int timeInSec, String[] ipList) {
        Map<String, Object> data = new HashMap<>();
        data.put("operator", operator.getDescription());
        data.put("param", param);
        data.put("ipList", ipList);
        data.put("timeInSec", timeInSec);
        String script = TemplateEngine.loadTemplate("scripts/network_to_script.sh", data);
        return CommandGenerator.generateCommand(script);
    }

    /**
     * https://lartc.org/howto/index.html
     * @param delayStr
     * @param timeInSec
     * @param ipList list of target ip address
     * @return CommandResult
     * @throws CommandExecutionException
     */
    public CommandResult networkDelayTo(String delayStr, int timeInSec, String[] ipList) throws CommandExecutionException {
        String host = getPrivateIp(getHost());
        LOG.info("network delay {} on {} to Ips {}", delayStr, host, ipList);
        String cmd = buildNetworkToCommand(NetEmOperator.DELAY, delayStr, timeInSec, ipList);
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
    }

    public CommandResult networkLossTo(String lossStr, int timeInSec, String[] ipList) throws CommandExecutionException {
        String host = getPrivateIp(getHost());
        LOG.info("network delay {} on {} to Ips {}", lossStr, host, ipList);
        String cmd = buildNetworkToCommand(NetEmOperator.LOSS, lossStr, timeInSec, ipList);
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
    }

    /**
     * @param loss: loss in percentage, e.g. 1%
     */
    @Override
    public CommandResult networkLossFor(String loss, int timeInSec) throws CommandExecutionException {
        String host = getPrivateIp(getHost());
        LOG.info("network delay {} on {}", loss, host);
        String cmd = buildNetworkCmd("loss", loss, timeInSec);
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), cmd);
    }

    /**
     * @param lossPercentage: loss in percentage, e.g. 1%
     */
    @Override
    public CommandResult networkLoss(String lossPercentage) throws CommandExecutionException {
        String host = getPrivateIp(getHost());
        LOG.info("network package loss  {} on {}", lossPercentage, host);
        String cmd = "sudo tc qdisc add dev $v root netem loss " + lossPercentage;
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), interfaceCmd(host) + cmd);
    }

    @Override
    public CommandResult recoverNetwork() throws CommandExecutionException {
        String host = getPrivateIp(getHost());
        LOG.info("recover network on {}", host);
        String tcCmd = "sudo tc qdisc del dev $v root netem";
        return CommandExecutionUtil.runPartitionerCommand(getCommandExecutor(), interfaceCmd(host) + tcCmd);
    }

    @Override
    public CommandResult stressCpuFor(int timeInSec) throws CommandExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CommandResult stressMemoryFor(double percentage, int timeInSec) throws CommandExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBastionHost() {
        return bastionHost;
    }

    @Override
    public void setBastionHost(String bastionHost) {
        this.bastionHost = bastionHost;
    }

    @Override
    public CommandResult installPackages(List<String> packages) throws CommandExecutionException {
        return new CommandResult();
    }

    @Override
    public void setMetaData(String key, Object value) {
        this.serverMetaData.setMetaData(key, value);
    }

    @Override
    public Object getMetaData(String key) {
        return this.serverMetaData.getMetaData(key);
    }

    @Override
    public Class<? extends CommandExecutor> getExecutorClass() {
        return executorClass;
    }

    @Override
    public void setExecutorClass(Class<? extends CommandExecutor> executorClass) {
        this.executorClass = executorClass;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder
                .append("[")
                .append(serviceName)
                .append("]-")
                .append(host);
        return builder.toString();
    }
}
