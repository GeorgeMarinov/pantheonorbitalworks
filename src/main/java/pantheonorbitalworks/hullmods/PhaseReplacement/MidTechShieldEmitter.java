package pantheonorbitalworks.hullmods.PhaseReplacement;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MidTechShieldEmitter extends BaseHullMod {

	public static final float ShieldArc = 270f;
	public static final float Upkeep = 0.3f;
	public static final float Efficiency = 1.0f;

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ShieldAPI shield = ship.getShield();
		if (shield == null) {
			ship.setShield(ShieldType.FRONT, Upkeep, Efficiency, ShieldArc);
		}
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return ShieldArc + "";
		if (index == 1) return Upkeep * 100 + "%";
		if (index == 2) return Efficiency + "";
        return null;
    }
}
