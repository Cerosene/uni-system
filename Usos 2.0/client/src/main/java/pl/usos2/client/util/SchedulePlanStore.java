package pl.usos2.client.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SchedulePlanStore {

    private static final Map<Long, Map<String, String>> PLAN_BY_GROUP = new ConcurrentHashMap<>();

    private SchedulePlanStore() {
    }

    public static Map<String, String> getPlanForGroup(Long groupId) {
        if (groupId == null) {
            return Collections.emptyMap();
        }
        Map<String, String> plan = PLAN_BY_GROUP.get(groupId);
        if (plan == null) {
            return Collections.emptyMap();
        }
        return new HashMap<>(plan);
    }

    public static void savePlanForGroup(Long groupId, Map<String, String> scheduleData) {
        if (groupId == null || scheduleData == null) {
            return;
        }
        PLAN_BY_GROUP.put(groupId, new HashMap<>(scheduleData));
    }
}
