package net.bowen.system.command;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private final List<Command> undoList = new ArrayList<>();
    private final List<Command> redoList = new ArrayList<>();
    private final int undoCount;

    public CommandManager(int undoCount) {
        this.undoCount = undoCount;
    }

    public void execute(Command cmd) {
        cmd.execute();
        undoList.add(cmd);

        // Remove the earliest cmd and leave the list size to "undoCount".
        if (undoList.size() > undoCount) {
            undoList.remove(0);
        }

        // Clear the redo list because the cmd can no longer be used now.
        redoList.clear();
    }

    public void undo() {
        if (undoList.isEmpty()) return;

        Command cmd = undoList.get(undoList.size() - 1);
        cmd.undo();

        undoList.remove(cmd);
        redoList.add(cmd);
    }

    public void redo() {
        if (redoList.isEmpty()) return;

        Command cmd = redoList.get(redoList.size() - 1);
        cmd.execute();

        redoList.remove(cmd);
        undoList.add(cmd);
    }
}
