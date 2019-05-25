package scheduling.core.input;

public class CapacityType {
    private String name;
    private double defaultRate;

    public CapacityType(String name, double defaultRate) {
        this.name = name;
        this.defaultRate = defaultRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDefaultRate() {
        return defaultRate;
    }

    public void setDefaultRate(int defaultRate) {
        this.defaultRate = defaultRate;
    }

    @Override
    public String toString() {
        return name;
    }
}
