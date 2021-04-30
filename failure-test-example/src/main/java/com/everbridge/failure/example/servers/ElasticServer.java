package org.failuretest.failure.example.servers;

import org.failuretest.failurecore.CommandExecutionException;
import org.failuretest.failurecore.annotations.ServiceType;
import org.failuretest.failurecore.servers.AbstractDockerServer;

@ServiceType(serviceName = "elasticsearch")
public class ElasticServer extends AbstractDockerServer {

    @Override
    protected String getContainerId() throws CommandExecutionException {
        // docker command can also use container name
        return getHost();
    }
}
