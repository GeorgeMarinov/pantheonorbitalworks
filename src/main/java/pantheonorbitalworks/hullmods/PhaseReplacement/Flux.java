package pantheonorbitalworks.hullmods.PhaseReplacement;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class Flux extends BaseHullMod {

	public int ExtraFlux = 30;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFluxCapacity().modifyPercent(id, ExtraFlux);
		stats.getFluxDissipation().modifyPercent(id, ExtraFlux);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return ExtraFlux + "%";
        return null;
    }
}
