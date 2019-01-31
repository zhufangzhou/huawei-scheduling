package scheduling.core.output;

import scheduling.core.input.Production;

public class PlannedProduction  {
    private Production production;
    private int lots;
    private int startDate;
    private int endDate;

    public PlannedProduction(Production production, int lots, int startDate, int endDate) {
        this.production = production;
        this.lots = lots;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Production getProduction() {
        return production;
    }

    public int getLots() {
        return lots;
    }

    public int getStartDate() {
        return startDate;
    }

    public int getEndDate() {
        return endDate;
    }

    public void addLots(int addedLots) {
        this.lots += addedLots;
    }

    public void removeLots(int removedLots) {
        this.lots -= removedLots;
    }

    public int getQuantity() {
        return lots * production.getLotSize();
    }
}
