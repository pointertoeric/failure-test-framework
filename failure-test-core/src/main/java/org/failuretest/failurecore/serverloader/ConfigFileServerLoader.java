package org.failuretest.failurecore.serverloader;

import org.failuretest.failurecore.ServerProcessor;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.servers.Server;
import org.failuretest.failurecore.ServerLoader;
import org.failuretest.failurecore.ServerRegistry;
import org.failuretest.failurecore.utils.PackageInstaller;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ConfigFileServerLoader implements ServerLoader {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigFileServerLoader.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String nodeFile;
    private TestContext testContext;
    private Properties config;
    private List<Server> nodes = new ArrayList<>();
    private ServerProcessor[] serverProcessors;
    private List<Server> targets = new ArrayList<>();

    public ConfigFileServerLoader(String nodeFile, TestContext testContext, ServerProcessor... serverProcessors) {
        this.testContext = testContext;
        this.nodeFile = nodeFile;
        this.serverProcessors = serverProcessors;
        this.config = testContext.getConfig();
    }

    @Override
    public List<Server> getServerList() {
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(
                    this.getClass().getClassLoader().getResourceAsStream(nodeFile));
            for (JsonNode jsonNode1 : jsonNode) {
                Class<? extends Server> serverClass = ServerRegistry.serviceNameServerMap.get(jsonNode1.get("serviceName").textValue());
                Server server = OBJECT_MAPPER.treeToValue(jsonNode1, serverClass);
                server.setBastionHost((String)config.getProperty("bastionHost"));
                server.setTestContext(testContext);
                server.setExecutorClass(testContext.getExecutorClass());
                for (ServerProcessor serverProcessor : serverProcessors) {
                    server = serverProcessor.process(jsonNode1.get("serviceName").textValue(), server);
                }
                nodes.add(server);

                Class<? extends Server> target = this.testContext.getFailModel().getPartitioner().getTarget();
                Class<? extends Server> sclass = ServerRegistry.serviceNameServerMap.get(server.getServiceName());
                if (target != null && sclass == target) {
                    targets.add(server);
                }
            }
            // install packages
            PackageInstaller.installConcurrently(testContext, targets);
        } catch (Exception e) {
            LOG.error("Load nodes error", e);

        }

        return nodes;
    }

    @Override
    public List<Server> getServerListByServiceName(String serviceName) {
        return getServerList().stream()
                .filter(node -> node.getServiceName().equalsIgnoreCase(serviceName))
                .collect(Collectors.toList());
    }
}
