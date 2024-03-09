package net.okt.system.command;

public interface Command {
    void execute();
    void undo();
}
