package com.googlecode.gdxquake2.gdxext;

public class ProgressTracker {
    public String action;
    public String file;
    public int total;
    public int processed;
    public final Runnable callback;

    public ProgressTracker(Runnable callback) {
        this.callback = callback;
    }
}
