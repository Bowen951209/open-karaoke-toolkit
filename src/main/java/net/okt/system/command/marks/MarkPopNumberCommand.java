package net.okt.system.command.marks;

import net.okt.system.command.Command;

import java.util.List;

public class MarkPopNumberCommand implements Command {
    private final List<Integer> markList;
    private final int q;
    private final int[] times;

    public MarkPopNumberCommand(List<Integer> markList, int q) {
        this.markList = markList;
        this.q = q;
        this.times = new int[q];
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
