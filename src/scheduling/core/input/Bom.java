package scheduling.core.input;

/**
 * The BOM of an item.
 * To produce an item, it includes how many of which components are required.
 */

public class Bom {
    private Item component;
    private int quantity;
    private SupplyType supplyType;

    public Bom(Item component, int quantity, SupplyType supplyType) {
        this.component = component;
        this.quantity = quantity;
        this.supplyType = supplyType;
    }

    public Item getComponent() {
        return component;
    }

    public int getQuantity() {
        return quantity;
    }

    public SupplyType getSupplyType() {
        return supplyType;
    }
}
