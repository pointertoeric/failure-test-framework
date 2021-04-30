package org.failuretest.failurecore.serverloader;

import org.failuretest.failurecore.ServerProcessor;
import org.failuretest.failurecore.TestContext;
import org.failuretest.failurecore.annotations.ServiceType;
import org.failuretest.failurecore.servers.AbstractNomadDockerServer;
import org.failuretest.failurecore.servers.NomadClientServer;
import org.failuretest.failurecore.servers.Server;
import org.failuretest.failurecore.ServerLoader;
import org.failuretest.failurecore.ServerRegistry;
import org.failuretest.failurecore.servers.DummyServer;
import org.failuretest.failurecore.utils.NomadClient;
import org.failuretest.failurecore.utils.PackageInstaller;
import com.hashicorp.nomad.apimodel.Allocation;
import com.hashicorp.nomad.apimodel.AllocationListStub;
import com.hashicorp.nomad.apimodel.Node;
import com.hashicorp.nomad.apimodel.NodeListStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class NomadServerLoader implements ServerLoader {
    private static final Logger LOG = LoggerFactory.getLogger(NomadServerLoader.class);

    private NomadClient nomadClient;
    private TestContext testContext;
    private Properties config;
    private ServerProcessor[] serverProcessors;
    private List<Server> targets = new ArrayList<>();

    public NomadServerLoader(TestContext testContext, ServerProcessor... serverProcessors) {
        this.testContext = testContext;
        this.config = testContext.getConfig();
        this.serverProcessors = serverProcessors;
        this.nomadClient = new NomadClient((String)this.config.get("nomadHost"));
    }

    private List<Server> getNomadClients() {
        List<Server> servers = new ArrayList<>();
        List<NodeListStub> nodeListStubs = this.nomadClient.getNodeList().getValue();
        for (NodeListStub nodeListStub : nodeListStubs) {
            if (nodeListStub.getStatus().equalsIgnoreCase("ready")) {
                NomadClientServer server = new NomadClientServer();
                server.setHost(nodeListStub.getAddress());
                server.setExecutorClass(testContext.getExecutorClass());
                server.setServiceName(NomadClientServer.class.getAnnotation(ServiceType.class).serviceName());
                server.setSshUser((String) config.getProperty("nomadSshUser"));
                server.setSshKeyFile((String) config.getProperty("nomadSshkey"));
                server.setBastionHost((String) config.getProperty("bastionHost"));
                server.setTestContext(testContext);
                servers.add(server);
                LOG.info("add nomad client {}, {}", server.getHost(), nodeListStub.getStatus());
            }
        }
        return servers;
    }

    @Override
    public List<Server> getServerList() {
        List<Server> servers = new ArrayList<>();
        List<AllocationListStub> allocationList = this.nomadClient.getAllocationList();
        // add nomad clients
        servers.addAll(getNomadClients());
        for (AllocationListStub allocationListStub : allocationList) {
            // only get running client
            if (allocationListStub.getClientStatus().equalsIgnoreCase("running")) {
                LOG.info("client status: {}", allocationListStub.getClientStatus());
                String serviceName = allocationListStub.getJobId();
                Node node = this.nomadClient.getNodeById(allocationListStub.getNodeId());
                String ip = node.getAttributes().get("unique.network.ip-address").split(":")[0];
                LOG.info("service {} ip {}", serviceName, ip);
                try {
                    Class<? extends Server> sclass = ServerRegistry.serviceNameServerMap.get(serviceName);
                    if (sclass != null) {
                        Server server = sclass.newInstance();
                        server.setHost(ip);
                        server.setServiceName(serviceName);
                        server.setSshUser((String) config.getProperty("nomadSshUser"));
                        server.setSshKeyFile((String) config.getProperty("nomadSshkey"));
                        server.setBastionHost((String) config.getProperty("bastionHost"));
                        server.setTestContext(testContext);
                        Class<? extends Server> target = this.testContext.getFailModel().getPartitioner().getTarget();
                        // we are using nomad loader, convert is safe
                        AbstractNomadDockerServer dockerServer =  (AbstractNomadDockerServer)server;
                        Allocation allocation = nomadClient
                                .getApiClient()
                                .getAllocationsApi()
                                .info(allocationListStub.getId())
                                .getValue();
                        dockerServer.setAllocation(allocation);
                        for (ServerProcessor serverProcessor : serverProcessors) {
                            dockerServer = (AbstractNomadDockerServer)serverProcessor.process(serviceName, dockerServer);
                        }
                        dockerServer.setExecutorClass(testContext.getExecutorClass());
                        dockerServer.init();
                        servers.add(dockerServer);
                        if (target != null && sclass == target) {
                            targets.add(server);
                        }
                    } else {
                        Server server = new DummyServer();
                        server.setServiceName("dummy-server");
                        servers.add(server);
                    }
                } catch (Exception e) {
                    LOG.error("load server error", e);
                    throw new IllegalArgumentException(e.getMessage());
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
                .filter(server -> server.getServiceName().equalsIgnoreCase(serviceName))
                .collect(Collectors.toList());
    }
}
