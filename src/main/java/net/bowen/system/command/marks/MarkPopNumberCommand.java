package net.bowen.system.command.marks;

import net.bowen.system.command.Command;

import java.util.List;

public class MarkPopNumberCommand implements Command {
    private final List<Long> markList;
    private final int q;
    private final long[] times;

    public MarkPopNumberCommand(List<Long> markList, int q) {
        this.markList = markList;
        this.q = q;
        this.times = new long[q];
    }

    @Override
    public void execute() {
        for (int i = 0; i < q; i++) {
            times[i] = markList.remove(markList.size() - 1);
        }
    }

    @Override
    public void undo() {
        for (int i = q; i > 0 ; i--) {
            markList.add(times[i - 1]);
        }
    }
}
