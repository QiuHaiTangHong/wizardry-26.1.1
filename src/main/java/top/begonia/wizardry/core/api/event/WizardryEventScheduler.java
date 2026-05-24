package top.begonia.wizardry.core.api.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class WizardryEventScheduler {
    private static final int WHEEL_SIZE = 1200;
    @SuppressWarnings("unchecked")
    private static final List<ScheduledTask>[] TIMING_WHEEL = new List[WHEEL_SIZE];
    private static int currentTickIndex = 0;

    static {
        for (int i = 0; i < WHEEL_SIZE; i++) {
            TIMING_WHEEL[i] = new LinkedList<>();
        }
    }

    private WizardryEventScheduler() {
    }

    public static void post(String eventName, Object context) {
        List<IWizardryAction> actions = WizardryEventCollector.dumpActions(eventName);
        for (IWizardryAction action : actions) {
            action.execute(context);
        }
    }

    public static synchronized void schedule(int delayTicks, Runnable timerTask) {
        schedule(delayTicks, timerTask, false);
    }

    public static synchronized void schedule(int delayTicks, Runnable timerTask, boolean async) {
        if (delayTicks <= 0) {
            if (async) {
                CompletableFuture.runAsync(timerTask);
            } else {
                timerTask.run();
            }
            return;
        }
        int targetIndex = (currentTickIndex + delayTicks) % WHEEL_SIZE;
        int rounds = delayTicks / WHEEL_SIZE;
        TIMING_WHEEL[targetIndex].add(new ScheduledTask(rounds, timerTask, async));
    }

    public static synchronized void pulse() {
        List<ScheduledTask> currentBucket = TIMING_WHEEL[currentTickIndex];
        if (!currentBucket.isEmpty()) {
            List<ScheduledTask> expiredTasks = new ArrayList<>();
            for (ScheduledTask task : currentBucket) {
                if (task.getRemainingRounds() <= 0) {
                    if (task.isAsync()) {
                        CompletableFuture.runAsync(task::run);
                    } else {
                        task.run();
                    }
                    expiredTasks.add(task);
                } else {
                    task.decrementRounds();
                }
            }
            currentBucket.removeAll(expiredTasks);
        }
        currentTickIndex = (currentTickIndex + 1) % WHEEL_SIZE;
    }

    public static synchronized void clearAll() {
        for (int i = 0; i < WHEEL_SIZE; i++) {
            TIMING_WHEEL[i].clear();
        }
        currentTickIndex = 0;
    }
}
