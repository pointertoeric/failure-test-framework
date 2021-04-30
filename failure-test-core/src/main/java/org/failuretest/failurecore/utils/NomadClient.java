package org.failuretest.failurecore.utils;

import com.hashicorp.nomad.apimodel.*;
import com.hashicorp.nomad.javasdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NomadClient {
    private static final Logger LOG = LoggerFactory.getLogger(NomadClient.class);

    private String nomadUrl;
    private NomadApiClient apiClient;

    public NomadClient(String nomadUrl) {
        this.nomadUrl = nomadUrl;
        NomadApiConfiguration config =
                new NomadApiConfiguration.Builder()
                        .setAddress(nomadUrl)
                        .build();

        apiClient = new NomadApiClient(config);
    }

    public NomadApiClient getApiClient() {
        return apiClient;
    }

    public ServerQueryResponse<List<JobListStub>> getJobList() {
        JobsApi jobsApi = apiClient.getJobsApi();
        ServerQueryResponse<List<JobListStub>> responseFuture = null;
        try {
            responseFuture = jobsApi.list();
        } catch (Exception e) {
            LOG.error("get job list error", e);
        }
        return responseFuture;
    }

    public ServerQueryResponse<List<NodeListStub>> getNodeList() {
        ServerQueryResponse<List<NodeListStub>> res = null;
        try {
            res = apiClient.getNodesApi().list();
        } catch (Exception e) {
            LOG.error("get node list error", e);
        }
        return res;
    }

    public Node getNodeById(String nodeId) {
        Node res = null;
        try {
            res = apiClient.getNodesApi().info(nodeId).getValue();
        } catch (Exception e) {
            LOG.error("error getting node by id: {}", nodeId, e);
        }
        return res;
    }

    public List<AllocationListStub> getAllocationList() {
        List<AllocationListStub> res = null;
        try {
            res = apiClient.getAllocationsApi().list().getValue();
        } catch (Exception e) {
            LOG.error("get allocation error", e);
        }
        return res;
    }

    public ClientApi getClientApi() {
        Node node = new Node();
        node.setHttpAddr(nomadUrl);
        ClientApi clientApi = apiClient.getClientApi(node);
        return clientApi;
    }

    public Resources getResources(Allocation allocation) {
        try {
            Resources resources = allocation.getResources();
            LOG.info("resource limit: {}", resources);
            return resources;
        } catch (Exception e) {
            LOG.error("get resource error", e);
        }
        return null;
    }

    public ResourceUsage getResourceUsage(Allocation allocation) {
        try {
            ClientApi clientApi = getClientApi();
            ResourceUsage usage = clientApi.stats(allocation.getId()).getValue().getResourceUsage();
            LOG.info("resource usage: {}", usage);
            return usage;
        } catch (Exception e) {
            LOG.error("get resource usage error", e);
        }
        return null;
    }
}
