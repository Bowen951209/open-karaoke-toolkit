package net.okt.system.command.marks;

import net.okt.system.command.Command;

import java.util.List;

public class MarkAddCommand implements Command {
    private final List<Integer> markList;
    private final int time;

    public MarkAddCommand(List<Integer> markList, int time) {
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
