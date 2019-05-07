package scheduling.core.input;

/**
 * A demand includes an ordered demand and a forecast demand.
 */

public class Demand {
    private long orderDemand;
    private long forecastDemand;
    private long delayed;

    public Demand(long orderDemand, long forecastDemand, long delayed) {
        this.orderDemand = orderDemand;
        this.forecastDemand = forecastDemand;
        this.delayed = delayed;
    }

    public Demand(long orderDemand, long forecastDemand) {
        this.orderDemand = orderDemand;
        this.forecastDemand = forecastDemand;
        this.delayed = orderDemand;
    }

    public long getOrderDemand() {
        return orderDemand;
    }

    public long getForecastDemand() {
        return forecastDemand;
    }

    public void setOrderDemand(long orderDemand) {
        this.orderDemand = orderDemand;
    }

    public void setForecastDemand(long forecastDemand) {
        this.forecastDemand = forecastDemand;
    }

    public void setDelayed(long delayed) {
        this.delayed = delayed;
    }

    @Override
    public String toString() {
        return "[" + orderDemand + ", " + forecastDemand + "]";
    }
}
