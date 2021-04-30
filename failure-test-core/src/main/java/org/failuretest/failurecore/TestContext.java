package org.failuretest.failurecore;

import org.failuretest.failurecore.annotations.ServiceType;
import org.failuretest.failurecore.executors.CommandExecutor;
import org.failuretest.failurecore.servers.Server;
import org.failuretest.failurecore.utils.FileTool;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * TestContext is used to load configurations and server definitions.
 * TODO: the name maybe confusing? maybe call it ApplicationContext?
 */
public class TestContext {
    private static final Logger LOG = LoggerFactory.getLogger(TestContext.class);

    private Properties config;
    private ServerLoader serverLoader;
    private List<String> requiredPackages = new ArrayList<>();
    private List<Action> failedActions = new ArrayList<>();
    private FailModel failModel;
    private Class<? extends CommandExecutor> executorClass;
    private String configFile = "config.properties";

    public TestContext() {
        String config = System.getProperty("config");
        if (config != null) {
            this.configFile = config;
        }
        loadAppConfig();
    }

    public TestContext(String configFile) {
        this.configFile = configFile;
        loadAppConfig();
    }

    public TestContext(String configFile, String... scanPackages) {
        this(configFile);
        scanComponents(scanPackages);
    }

        /**
         * @param scanPackages: A list of package name to be scanned for Servers.
         */
    public TestContext(String... scanPackages) {
        this();
        // scan package path to register Servers
        scanComponents(scanPackages);
    }

    private void scanComponents(String[] scanPackages) {
        for (String packageName : scanPackages) {
            Reflections reflections = new Reflections(packageName);
            Set<Class<?>> annotatedClassSet = reflections.getTypesAnnotatedWith(ServiceType.class);
            annotatedClassSet.forEach(
                    aClass -> {
                        if (aClass.getAnnotation(ServiceType.class).alias().equalsIgnoreCase("")) {
                            ServerRegistry.registerService(
                                    aClass.getAnnotation(ServiceType.class).serviceName(),
                                    (Class<? extends Server>) aClass);
                        } else {
                            ServerRegistry.registerService(
                                    aClass.getAnnotation(ServiceType.class).alias(),
                                    (Class<? extends Server>) aClass);
                        }
                    }
            );
        }
    }

    public void registerService(String serviceName, Class<? extends Server> sclass) {
        ServerRegistry.registerService(serviceName, sclass);
    }

    public Properties getConfig() {
        return this.config;
    }

    public ServerLoader getServerLoader() {
        return serverLoader;
    }

    public void setServerLoader(ServerLoader serverLoader) {
        this.serverLoader = serverLoader;
    }

    private void loadAppConfig() {
        try {
            this.config = FileTool.loadProperties(this.configFile);
        } catch (IOException e) {
            LOG.error("Load app config error {}", e);

        }
    }

    public void setRequiredPackages(List<String> requiredPackages) {
        this.requiredPackages = requiredPackages;
    }

    public List<String> getRequiredPackages() {
        return requiredPackages;
    }

    public void addFailedAction(Action action) {
        if (action.isNeedCleanUp()) {
            this.failedActions.add(action);
        }
    }

    public FailModel getFailModel() {
        return failModel;
    }

    public void setFailModel(FailModel failModel) {
        this.failModel = failModel;
    }

    public Class<? extends CommandExecutor> getExecutorClass() {
        return executorClass;
    }

    public void setExecutorClass(Class<? extends CommandExecutor> executorClass) {
        this.executorClass = executorClass;
    }

    public void tearDown() {
        for (Action action : this.failedActions) {
            LOG.info("Action execution failed: {}", action);
            LOG.info("Rerun the Action....");
            try {
                action.perform();
            } catch (CommandExecutionException e) {
                LOG.error("action {} failed again", action, e);
            }
        }
    }
}
