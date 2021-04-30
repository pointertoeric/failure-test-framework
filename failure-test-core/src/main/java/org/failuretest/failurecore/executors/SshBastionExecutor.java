package org.failuretest.failurecore.executors;

import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.utils.FileTool;
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshBastionExecutor implements CommandExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(SshBastionExecutor.class);

    private String bastionHost;
    private String keyFile;
    private String sshUser;
    private String host;
    private Ssh ssh;

    public SshBastionExecutor(String bastionHost, String host, String keyFile, String sshUser) {
        this.bastionHost = bastionHost;
        this.sshUser = sshUser;
        this.keyFile = keyFile;
        this.host = host;
        try {
            String key = FileTool.readFile(keyFile);
            ssh = new Ssh(bastionHost, sshUser, key);
        } catch (Exception e) {
            LOG.error("create ssh client error", e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public String executeCommand(String command) throws CommandExecutionException {
        try {
            String cmd = "ssh -o StrictHostKeyChecking=no " + host + " '" + command + "'";
            LOG.info("execute command {}", cmd);
            String stdout = new Shell.Plain(ssh).exec(cmd);
            LOG.info("command result {}", stdout);
            return stdout;
        } catch (Exception e) {
            LOG.error("ssh error for", e);
            throw new CommandExecutionException("ssh command error");
        }

    }
}
