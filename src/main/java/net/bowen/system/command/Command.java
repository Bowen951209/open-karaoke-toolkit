package net.bowen.system.command;

public interface Command {
    void execute();
    void undo();
}
