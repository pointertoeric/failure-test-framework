package org.failuretest.failurecore;

import org.failuretest.failurecore.servers.Server;

import java.util.List;

public interface ServerLoader {

    List<Server> getServerList();

    List<Server> getServerListByServiceName(String serviceName);

}
