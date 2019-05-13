package scheduling.core.input;

public class Transit {
    private Item item;
    private Plant fromPlant;
    private Plant toPlant;
    private int cost;
    private int leadTime;

    public Transit(Item item, Plant fromPlant, Plant toPlant, int cost, int leadTime) {
        this.item = item;
        this.fromPlant = fromPlant;
        this.toPlant = toPlant;
        this.cost = cost;
        this.leadTime = leadTime;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Plant getFromPlant() {
        return fromPlant;
    }

    public void setFromPlant(Plant fromPlant) {
        this.fromPlant = fromPlant;
    }

    public Plant getToPlant() {
        return toPlant;
    }

    public void setToPlant(Plant toPlant) {
        this.toPlant = toPlant;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getLeadTime() {
        return leadTime;
    }

    public void setLeadTime(int leadTime) {
        this.leadTime = leadTime;
    }

    @Override
    public String toString() {
        return "Transit{" +
                item +
                ", " + fromPlant.toString() +
                " -> " + toPlant.toString() +
                ", " + cost +
                ", " + leadTime +
                '}';
    }
}
