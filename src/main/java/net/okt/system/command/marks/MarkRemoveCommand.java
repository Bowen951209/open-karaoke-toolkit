package net.okt.system.command.marks;

import net.okt.system.command.Command;

import java.util.List;

public class MarkRemoveCommand implements Command {
    private final List<Long> markList;
    private final int i;
    private final long time;

    public MarkRemoveCommand(List<Long> markList, int index) {
        this.markList = markList;
        this.i = index;
        this.time = markList.get(i);
    }

    @Override
    public void execute() {
        markList.remove(i);
    }

    @Override
    public void undo() {
        markList.add(i, time);
    }
}
