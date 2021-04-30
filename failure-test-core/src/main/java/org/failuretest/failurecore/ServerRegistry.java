package org.failuretest.failurecore;

import org.failuretest.failurecore.servers.DummyServer;
import org.failuretest.failurecore.servers.NomadClientServer;
import org.failuretest.failurecore.servers.Server;

import java.util.HashMap;
import java.util.Map;

/**
 * For each new Server please put an entry here, the key is serviceName, should be same with the ServiceName
 * in your nodes configuration file
 */
public class ServerRegistry {
    public static Map<String, Class<? extends Server>> serviceNameServerMap = new HashMap<String, Class<? extends Server>>() {
        {
            put("dummy-server", DummyServer.class);
            put("nomad-client", NomadClientServer.class);
        }
    };

    public static void registerService(String serviceName, Class<? extends Server> sclass) {
        ServerRegistry.serviceNameServerMap.put(serviceName, sclass);
    }

    public static String getServiceNameByClass(Class<? extends Server> sClass) {
        for (Map.Entry<String, Class<? extends Server> > entry : serviceNameServerMap.entrySet()) {
            if (entry.getValue() == sClass) {
                return entry.getKey();
            }
        }

        return "dummy-server";
    }


}
