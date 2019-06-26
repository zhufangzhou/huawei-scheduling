package scheduling.core.input;

/**
 * A BOM component contains the material, quantity of the material, and supply type.
 */

public class BomComponent {
    private Item material;
    private double quantity;
    private SupplyType supplyType;

    public BomComponent(Item material, double quantity, SupplyType supplyType) {
        this.material = material;
        this.quantity = quantity;
        this.supplyType = supplyType;
    }

    public Item getMaterial() {
        return material;
    }

    public double getQuantity() {
        return quantity;
    }

    public SupplyType getSupplyType() {
        return supplyType;
    }

    @Override
    public String toString() {
        return "[" + material.toString() + ", " + quantity + "]";
    }
}
