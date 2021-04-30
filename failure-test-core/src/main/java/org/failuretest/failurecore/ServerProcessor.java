package org.failuretest.failurecore;

import org.failuretest.failurecore.servers.Server;

/**
 * ServerProcessor can be passed to ServerLoader for further process
 * passed Server instance can be registered server instance or DummyServer
 * in case ServerLoader does not know how to instantiate server instance,
 * leave user to handle it.
 */
public interface ServerProcessor {

    Server process(String serviceName, Server server);

}
