package org.failuretest.failurecore.executors;

import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.utils.FileTool;
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshExecutor implements CommandExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(SshExecutor.class);

    private String keyFile;
    private String sshUser;
    private String host;
    private Ssh ssh;

    public SshExecutor(String host, String keyFile, String sshUser) {
        this.sshUser = sshUser;
        this.keyFile = keyFile;
        this.host = host;
        try {
            String key = FileTool.readFile(keyFile);
            ssh = new Ssh(host, sshUser, key);
        } catch (Exception e) {
            LOG.error("create ssh client error", e);
            throw new RuntimeException("create ssh client error");
        }
    }

    @Override
    public String executeCommand(String command) throws CommandExecutionException {
        try {
            return new Shell.Plain(ssh).exec(command);
        } catch (Exception e) {
            LOG.error("ssh error for command: {}", command, e);
            throw new CommandExecutionException("ssh command error");
        }

    }
}
