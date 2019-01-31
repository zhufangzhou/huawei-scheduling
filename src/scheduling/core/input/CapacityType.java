package scheduling.core.input;

import java.util.HashMap;
import java.util.Map;

public enum CapacityType {
    PCS("Pcs"),
    POINT("Point");

    private final String name;

    CapacityType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Reverse-lookup map
    private static final Map<String, CapacityType> lookup = new HashMap<>();

    static {
        for (CapacityType a : CapacityType.values()) {
            lookup.put(a.getName(), a);
        }
    }

    public static CapacityType get(String name) {
        return lookup.get(name);
    }
}
