package net.okt.system.command.marks;

import net.okt.system.command.Command;

import java.util.Collections;
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
        boolean shouldSort = !markList.isEmpty() && time < markList.get(markList.size() - 1);
        markList.add(time);
        if (shouldSort)
            Collections.sort(markList);
    }

    @Override
    public void undo() {
        markList.remove((Integer) time);
    }
}
