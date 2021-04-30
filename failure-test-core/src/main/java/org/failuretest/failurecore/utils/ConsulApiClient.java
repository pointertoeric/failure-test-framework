package org.failuretest.failurecore.utils;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.catalog.CatalogNodesRequest;
import com.ecwid.consul.v1.catalog.model.CatalogNode;
import com.ecwid.consul.v1.catalog.model.Node;
import com.ecwid.consul.v1.coordinate.model.Datacenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ConsulApiClient {
    private static final Logger LOG = LoggerFactory.getLogger(ConsulApiClient.class);

    private String consulUrl;
    private ConsulClient client;

    public ConsulApiClient(String consulUrl) {
        this.consulUrl = consulUrl;
        this.client = new ConsulClient(this.consulUrl);
    }

    public List<Datacenter> getDataCenters() {
        return this.client.getDatacenters().getValue();
    }

    public List<Node> getNodes(String dc) {
        return this.client.getCatalogNodes(CatalogNodesRequest.newBuilder().setDatacenter(dc).build()).getValue();
    }

    public CatalogNode getNodeInfo(String dc, String nodeName) {
        return this.client.getCatalogNode(nodeName, QueryParams.Builder.builder().setDatacenter(dc).build()).getValue();
    }
}
