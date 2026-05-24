package top.begonia.wizardry.core.api.event;

import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class WizardryEventCollector {
    private static final Map<String, Map<UUID, IWizardryAction>> DYNAMIC_COLLECTOR = new ConcurrentHashMap<>();

    public static @NonNull UUID collectOnDemand(String eventName, IWizardryAction action) {
        UUID token = UUID.randomUUID();
        DYNAMIC_COLLECTOR.computeIfAbsent(eventName, k -> new ConcurrentHashMap<>()).put(token, action);
        return token;
    }

    public static void release(String eventName, UUID token) {
        Map<UUID, IWizardryAction> actionsMap = DYNAMIC_COLLECTOR.get(eventName);
        if (actionsMap != null) {
            actionsMap.remove(token);
            if (actionsMap.isEmpty()) {
                DYNAMIC_COLLECTOR.remove(eventName);
            }
        }
    }

    public static void collectDelayed(String eventName, int delayTicks, Object context) {
        WizardryEventScheduler.schedule(delayTicks, () -> WizardryEventScheduler.post(eventName, context));
    }

    public static @NonNull List<IWizardryAction> dumpActions(String eventName) {
        Map<UUID, IWizardryAction> actionsMap = DYNAMIC_COLLECTOR.get(eventName);
        if (actionsMap == null || actionsMap.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(actionsMap.values());
    }

    public static void clearAll() {
        DYNAMIC_COLLECTOR.clear();
    }
}
