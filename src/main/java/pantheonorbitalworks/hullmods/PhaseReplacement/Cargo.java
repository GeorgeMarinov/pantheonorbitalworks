package pantheonorbitalworks.hullmods.PhaseReplacement;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class Cargo extends BaseHullMod {

	public int ExtraCargo = 150;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getCargoMod().modifyPercent(id, ExtraCargo);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return ExtraCargo + "%";
        return null;
    }
}
