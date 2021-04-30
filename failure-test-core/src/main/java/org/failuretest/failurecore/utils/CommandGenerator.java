package org.failuretest.failurecore.utils;

public class CommandGenerator {

    private CommandGenerator() {}

    public static String generateCommand(String commandScipt) {
        String cmd = String.format(
                "echo -e %s > /tmp/failure-test.sh; nohup sh /tmp/failure-test.sh > /dev/null &", commandScipt);
        return cmd;
    }
}
