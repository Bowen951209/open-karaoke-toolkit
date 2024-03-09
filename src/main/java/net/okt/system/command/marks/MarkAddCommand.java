package net.okt.system.command.marks;

import net.okt.system.command.Command;

import java.util.List;

public class MarkAddCommand implements Command {
    private final List<Long> markList;
    private final long time;

    public MarkAddCommand(List<Long> markList, long time) {
        this.markList = markList;
        this.time = time;
    }

    @Override
    public void execute() {
        markList.add(time);
    }

    @Override
    public void undo() {
        markList.remove(time);
    }
}
