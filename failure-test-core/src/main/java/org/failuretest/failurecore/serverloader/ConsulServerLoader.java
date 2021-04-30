package org.failuretest.failurecore.serverloader;

import com.ecwid.consul.v1.catalog.model.CatalogNode;
import com.ecwid.consul.v1.catalog.model.Node;
import com.ecwid.consul.v1.coordinate.model.Datacenter;
import org.failuretest.failurecore.ServerLoader;
import org.failuretest.failurecore.ServerProcessor;
import org.failuretest.failurecore.ServerRegistry;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.annotations.ServiceType;
import org.failuretest.failurecore.servers.DummyServer;
import org.failuretest.failurecore.servers.Server;
import org.failuretest.failurecore.utils.ConsulApiClient;
import org.failuretest.failurecore.utils.PackageInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class ConsulServerLoader implements ServerLoader {
    private static final Logger LOG = LoggerFactory.getLogger(ConsulServerLoader.class);

    private ConsulApiClient consulApiClient;
    private TestContext testContext;
    private Properties config;
    private ServerProcessor[] serverProcessors;
    private List<Server> targets = new ArrayList<>();

    public ConsulServerLoader(TestContext testContext, ServerProcessor... serverProcessors) {
        this.testContext = testContext;
        this.config = testContext.getConfig();
        this.serverProcessors = serverProcessors;
        this.consulApiClient = new ConsulApiClient((String)this.config.get("consulUrl"));
    }

    @Override
    public List<Server> getServerList() {
        List<Server> servers = new ArrayList<>();
        // get data centers
        List<Datacenter> datacenters = consulApiClient.getDataCenters();
        // for each data center find all nodes
        for (Datacenter datacenter : datacenters) {
            List<Node> nodes = consulApiClient.getNodes(datacenter.getDatacenter());
            for (Node node : nodes) {
                CatalogNode catalogNode = consulApiClient.getNodeInfo(datacenter.getDatacenter(), node.getNode());
                Map<String, CatalogNode.Service> serviceMap = catalogNode.getServices();
                // for each node get all the services
                for (Map.Entry<String, CatalogNode.Service> entry : serviceMap.entrySet()) {
                    LOG.info("service name: {}", entry.getKey());
                    CatalogNode.Service service = entry.getValue();
                    LOG.info("service {}", service);
                    String serviceName = service.getService();
                    Class<? extends Server> sclass = ServerRegistry.serviceNameServerMap.get(serviceName);
                    try {
                        Class<? extends Server> target = this.testContext.getFailModel().getPartitioner().getTarget();
                        Server server = null;
                        if (sclass != null) {
                            server = sclass.newInstance();
                        } else {
                            // sclass is null maybe due to serviceName is different from registered name
                            // call processor do the job, use dummy server to save some information
                            server = new DummyServer();
                        }
                        server.setHost(node.getNode());
                        server.setServiceName(serviceName);
                        server.setTestContext(testContext);
                        server.setMetaData("dataCenter", datacenter.getDatacenter());
                        server.setExecutorClass(testContext.getExecutorClass());
                        for (ServerProcessor serverProcessor : serverProcessors) {
                            server = serverProcessor.process(service.getService(), server);
                        }
                        if ( server != null && server.getClass() != DummyServer.class) {
                            server.init();
                            servers.add(server);
                            if (target != null && sclass == target) {
                                targets.add(server);
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("load server error", e);
                        throw new IllegalArgumentException(e.getMessage());
                    }

                }

            }
        }
        // install packages
        PackageInstaller.installConcurrently(testContext, targets);
        return servers;
    }

    @Override
    public List<Server> getServerListByServiceName(String serviceName) {
        return getServerList().stream()
                .filter(server -> {
                    String lookupName = server.getClass().getAnnotation(ServiceType.class).alias();
                    if (lookupName.equalsIgnoreCase("")) {
                        return server.getServiceName().equalsIgnoreCase(serviceName);
                    } else {
                        return lookupName.equalsIgnoreCase(serviceName);
                    }
                })
                .collect(Collectors.toList());
    }
}
