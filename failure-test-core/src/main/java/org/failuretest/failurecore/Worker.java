package org.failuretest.failurecore;

import java.util.concurrent.Callable;

/**
 * Worker is base class of Client and partitioner
 */
public abstract class Worker implements Callable<Long> {
    private ClientContext clientContext;
    private String name;

    public Worker(String name) {
        this.clientContext = new ClientContext();
        this.name = name;
    }

    public abstract Long call();

    public ClientContext getClientContext() {
        return clientContext;
    }

    public void setClientContext(ClientContext clientContext) {
        this.clientContext = clientContext;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
