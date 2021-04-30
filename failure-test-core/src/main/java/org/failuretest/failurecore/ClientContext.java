package org.failuretest.failurecore;

/**
 * ClientContext is used to store execution result of actions, maybe used by actions, models to validate
 * result.
 */
public class ClientContext {
    private Object result;

    public ClientContext() {
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
