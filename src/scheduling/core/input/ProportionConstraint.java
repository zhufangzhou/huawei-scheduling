package scheduling.core.input;

import scheduling.core.input.Plant;

import java.util.HashMap;
import java.util.Map;

public class ProportionConstraint {
    private Map<Plant, PlantProportionConstraint> pcMap;

    public ProportionConstraint() {
        pcMap = new HashMap<>();
    }

    public void putProportion(Plant plant, int proportion, int startDate, int endDate, int cumulateProd) {
        pcMap.put(plant, new PlantProportionConstraint(proportion, startDate, endDate, cumulateProd));
    }

    public int getPlantProportion(Plant plant) {
        return pcMap.get(plant).proportion;
    }

    public int getPlantStartDate(Plant plant) {
        return pcMap.get(plant).startDate;
    }

    public int getPlantEndDate(Plant plant) {
        return pcMap.get(plant).endDate;
    }

    public int getPlantCumulateProd(Plant plant) {
        return pcMap.get(plant).cumulateProd;
    }

    public class PlantProportionConstraint {
        private int proportion;
        private int startDate;
        private int endDate;
        private int cumulateProd;

        public PlantProportionConstraint(int proportion, int startDate, int endDate, int cumulateProd) {
            this.proportion = proportion;
            this.startDate = startDate;
            this.endDate = endDate;
            this.cumulateProd = cumulateProd;
        }
    }
}
