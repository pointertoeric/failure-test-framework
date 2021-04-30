package org.failuretest.failurecore.executors;

import org.failuretest.failurecore.CommandExecutionException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;

public class SshPasswordExecutor implements CommandExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(SshPasswordExecutor.class);

    private String sshUser;
    private String sshPassword;
    private String host;
    private JSch jsch;
    private static final int PORT = 22;

    public SshPasswordExecutor(String host, String sshUser, String sshPassword) {
        this.sshUser = sshUser;
        this.sshPassword = sshPassword;
        this.host = host;
        try {
            jsch = new JSch();
        } catch (Exception e) {
            LOG.error("create ssh client error", e);
            throw new RuntimeException("create ssh client error");
        }
    }

    /**
     * ssh by password, have to use "sudo -S", so need to provide password from input stream
     * @return String
     * @throws CommandExecutionException
     */
    @Override
    public String executeCommand(String command) throws CommandExecutionException {
        try {
            LOG.info("run ssh command {}", command);
            Session session = jsch.getSession(sshUser, host, PORT);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setPassword(sshPassword);
            session.setConfig(config);
            session.connect();
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            try (InputStream in = channel.getInputStream();
                 OutputStream out = channel.getOutputStream()) {
                channel.connect();

                out.write((sshPassword + "\n").getBytes());
                out.flush();

                String res = "";
                byte[] tmp = new byte[1024];
                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(tmp, 0, 1024);
                        if (i < 0) break;
                        res = new String(tmp, 0, i);
                        LOG.info("command result {}", res);
                    }
                    if (channel.isClosed()) {
                        LOG.info("exit-status: {}", channel.getExitStatus());
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }
                channel.disconnect();
                session.disconnect();
                LOG.info("command result {}", res);
                return res;
            }
        }catch (Exception e) {
            LOG.error("ssh error for command: {}", command, e);
            throw new CommandExecutionException("ssh command error");
        }

    }
}
