package org.failuretest.failurecore.actions;

import org.failuretest.failurecore.Action;
import org.failuretest.failurecore.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class WaitAction extends Action {

    private static final Logger LOG = LoggerFactory.getLogger(WaitAction.class);

    private double waitTime;
    private JitterType jitterType;

    public static enum JitterType {
        FIXED,
        GAUSS
    }

    public WaitAction(Integer waitTime) {
        this(waitTime, JitterType.FIXED);
    }

    public WaitAction(Integer waitTime, JitterType jitterType) {
        super(waitTime, jitterType);
        this.waitTime = waitTime;
        this.jitterType = jitterType;
    }

    /**
     * here we set 2 * std ~ 50% * mean
     * x = z * std + u
     * tune the variance if want to sample more wide range of values
     */
    private double sampleFromGauss(double mean) {
        //
        //
        Random random = new Random();
        double std = mean * 0.25;
        double sample = random.nextGaussian() * std + mean;
        if (sample <= 0) {
            return 0.2;
        } else {
            return sample;
        }
    }

    private double computeWaitTime(double waitTime, JitterType jitterType) {
        switch (jitterType) {
            case FIXED: return waitTime;
            case GAUSS: return sampleFromGauss(waitTime);
        }
        // just for safeguard, wait at least 0.2s
        return 0.2;
    }

    @Override
    public void init(TestContext testContext) {
    }

    @Override
    public void perform() {
        double computeWaitTime = computeWaitTime(waitTime, jitterType);
        try {
            LOG.info("wait for {} sec...", computeWaitTime);
            Thread.sleep((long)computeWaitTime * 1000);
        } catch (Exception e) {
            LOG.error("sleeping error", e);
        }
    }
}
