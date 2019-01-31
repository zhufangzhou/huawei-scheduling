package scheduling.core.input;

import java.util.HashMap;
import java.util.Map;

public enum ItemType {
    RAW_MATERIAL("Raw Material"),
    FORECAST_COMPONENT("Forecast Component"),
    ORDER_ONLY("Order Only"),
    NORMAL("Normal"),
    TYPE4_COMPONENT("Type4 Component");

    private final String name;

    ItemType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Reverse-lookup map
    private static final Map<String, ItemType> lookup = new HashMap<>();

    static {
        for (ItemType a : ItemType.values()) {
            lookup.put(a.getName(), a);
        }
    }

    public static ItemType get(String name) {
        return lookup.get(name);
    }
}
