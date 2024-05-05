package net.okt.system.command.marks;

import net.okt.system.command.Command;

import java.util.List;

public class MarkSetCommand implements Command {
    private final List<Integer> markList;
    private final int i;
    private final int oTime;
    private final int time;

    public MarkSetCommand(List<Integer> markList, int i, int time) {
        this.markList = markList;
        this.i = i;
        this.time = time;
        this.oTime = markList.get(i);
    }

    @Override
    public void execute() {
        markList.set(i, time);
    }

    @Override
    public void undo() {
        markList.set(i, oTime);
    }
}
