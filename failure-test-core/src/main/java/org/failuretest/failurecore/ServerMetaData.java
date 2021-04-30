package org.failuretest.failurecore;

import java.util.HashMap;
import java.util.Map;

public class ServerMetaData {
    private Map<String, Object> metaData;

    public ServerMetaData() {
        this.metaData = new HashMap<>();
    }

    public void setMetaData(String key, Object value) {
        this.metaData.put(key, value);
    }

    public Object getMetaData(String key) {
        return this.metaData.get(key);
    }
}
