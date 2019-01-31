package scheduling.core.input;

/**
 * A demand includes an ordered demand and a forecast demand.
 */

public class Demand {
    private int orderDemand;
    private int forecastDemand;
    private int delayed;

    public Demand(int orderDemand, int forecastDemand, int delayed) {
        this.orderDemand = orderDemand;
        this.forecastDemand = forecastDemand;
        this.delayed = delayed;
    }

    public Demand(int orderDemand, int forecastDemand) {
        this.orderDemand = orderDemand;
        this.forecastDemand = forecastDemand;
        this.delayed = orderDemand;
    }

    public int getOrderDemand() {
        return orderDemand;
    }

    public int getForecastDemand() {
        return forecastDemand;
    }

    public void setOrderDemand(int orderDemand) {
        this.orderDemand = orderDemand;
    }

    public void setForecastDemand(int forecastDemand) {
        this.forecastDemand = forecastDemand;
    }

    public void setDelayed(int delayed) {
        this.delayed = delayed;
    }

    @Override
    public String toString() {
        return "[" + orderDemand + ", " + forecastDemand + "]";
    }
}
