package top.begonia.wizardry.core.api.event;

public class ScheduledTask {
    private int remainingRounds;
    private final Runnable action;
    private final boolean isAsync;

    public ScheduledTask(int remainingRounds, Runnable action, boolean isAsync) {
        this.remainingRounds = remainingRounds;
        this.action = action;
        this.isAsync = isAsync;
    }

    public int getRemainingRounds() {
        return this.remainingRounds;
    }

    public void decrementRounds() {
        this.remainingRounds--;
    }

    public boolean isAsync() {
        return this.isAsync;
    }

    public void run() {
        this.action.run();
    }
}
