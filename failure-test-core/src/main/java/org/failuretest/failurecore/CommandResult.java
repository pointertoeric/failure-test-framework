package org.failuretest.failurecore;

public class CommandResult {

    private String commandOutput;

    public static final CommandResult EMPTY_RESULT = new CommandResult();

    public CommandResult() {

    }

    public void setCommandOutput(String commandOutput) {
        this.commandOutput = commandOutput;
    }

    public String getCommandOutput() {
        return commandOutput;
    }

    public static CommandResult withCommandOutput(String output) {
        CommandResult result = new CommandResult();
        result.setCommandOutput(output);
        return result;
    }

}
