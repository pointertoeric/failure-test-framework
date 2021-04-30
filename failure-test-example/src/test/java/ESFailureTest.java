import org.failuretest.failure.example.LocalDockerModel;
import org.failuretest.failure.example.actions.WriteAction;
import org.failuretest.failure.example.servers.ElasticServer;
import org.failuretest.failurecore.*;
import org.failuretest.failurecore.*;
import org.failuretest.failurecore.actions.KillAction;
import org.failuretest.failurecore.actions.StartAction;
import org.failuretest.failurecore.actions.StressCpuForAction;
import org.failuretest.failurecore.actions.WaitAction;
import org.failuretest.failurecore.executors.LocalExecutor;
import org.failuretest.failurecore.serverloader.ConfigFileServerLoader;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESFailureTest {
    private static final Logger LOG = LoggerFactory.getLogger(ESFailureTest.class);


    private TestContext testContext;

    @Before
    public void setUp() {
        testContext = new TestContext("org.failuretest.failure.example");
        ServerLoader serverLoader = new ConfigFileServerLoader("node.json", testContext);
        testContext.setServerLoader(serverLoader);
        testContext.setExecutorClass(LocalExecutor.class);
    }

    @Test
    public void killESOnce() {
        ClientBuilder clientBuilder = new ClientBuilder(testContext);
        clientBuilder
                .actionType(ActionType.REPEAT)
                .concurrency(10)
                .duration(30)
                .action(new WriteAction())
                .action(new WaitAction(1))
                .build();
        PartitionerBuilder partitionerBuilder = new PartitionerBuilder(testContext);
        partitionerBuilder
                .target(ElasticServer.class)
                .partitionType(PartitionType.RANDOM)
                .actionType(ActionType.ONCE)
                .action(new WaitAction(5))
                .action(new KillAction())
                .action(new WaitAction(30))
                .action(new StartAction())
                .build();
        FailModel model = new LocalDockerModel(testContext, clientBuilder, partitionerBuilder);
        model.run();
        ModelVerificationHelper.showTimeLine();
        model.validate();
    }

    @Test
    public void stressForCPU() {
        ClientBuilder clientBuilder = new ClientBuilder(testContext);
        clientBuilder
                .actionType(ActionType.REPEAT)
                .concurrency(10)
                .duration(30)
                .action(new WriteAction())
                .action(new WaitAction(1))
                .build();
        PartitionerBuilder partitionerBuilder = new PartitionerBuilder(testContext);
        partitionerBuilder
                .target(ElasticServer.class)
                .partitionType(PartitionType.RANDOM)
                .actionType(ActionType.ONCE)
                .action(new WaitAction(5))
                .action(new StressCpuForAction(20))
                .build();
        FailModel model = new LocalDockerModel(testContext, clientBuilder, partitionerBuilder);
        model.run();
        ModelVerificationHelper.showTimeLine();
        model.validate();
    }
}
