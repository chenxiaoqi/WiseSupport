package com.lazyman.timetennis.task;

import java.util.ArrayList;
import java.util.List;

public class CompositeRunnable implements Runnable {

    private List<Runnable> runs = new ArrayList<>();

    public CompositeRunnable(List<Runnable> runs) {
        this.runs = runs;
    }

    @Override
    public void run() {
        for (Runnable run : runs) {
            run.run();
        }
    }
}
