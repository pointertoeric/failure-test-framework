package org.failuretest.failure.example.actions;

import org.failuretest.failurecore.Action;
import org.failuretest.failurecore.TestContext;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class WriteAction extends Action {
    private static final Logger LOG = LoggerFactory.getLogger(WriteAction.class);

    private RestHighLevelClient client;

    @Override
    public void init(TestContext testContext) {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));
    }

    @Override
    public void perform() {
        IndexRequest request = new IndexRequest("posts", "doc");
        String jsonString = "{" +
                "\"message\":\"" + UUID.randomUUID().toString() + "\"}";
        request.source(jsonString, XContentType.JSON);
        try {
            IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
            getActor().getClientContext().setResult(indexResponse);
            LOG.info("id {}", indexResponse);
        } catch (Exception e) {
            LOG.error("error ", e);
            getActor().getClientContext().setResult(e);
        }
    }
}
