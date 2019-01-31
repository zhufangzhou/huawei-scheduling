package scheduling.core.input;

import java.util.HashMap;
import java.util.Map;

public enum SupplyType {
    PUSH("Push"),
    BULK("Bulk"),
    SUPPLIER("Supplier");

    private final String name;

    SupplyType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Reverse-lookup map
    private static final Map<String, SupplyType> lookup = new HashMap<>();

    static {
        for (SupplyType a : SupplyType.values()) {
            lookup.put(a.getName(), a);
        }
    }

    public static SupplyType get(String name) {
        return lookup.get(name);
    }
}
