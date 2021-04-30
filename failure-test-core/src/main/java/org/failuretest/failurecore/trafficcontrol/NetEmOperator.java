package org.failuretest.failurecore.trafficcontrol;

/**
 * @see <a href="http://man7.org/linux/man-pages/man8/tc-netem.8.html">tc-netem</a>
 */
public enum NetEmOperator {
    DELAY("delay"),
    LOSS("loss"),
    LIMIT("limit"),
    CORRUPT("corrupt"),
    DUPLICATION("duplicate"),
    REORDERING("reorder"),
    RATE("rate"),
    SLOT("slot");

    private String description;

    NetEmOperator(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
