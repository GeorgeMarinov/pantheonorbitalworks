package pantheonorbitalworks.hullmods.PhaseReplacement;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MidTechShieldEmitter extends BaseHullMod {

	public static final float ShieldArc = 210f;
	public static final float Efficiency = 1f;

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ShieldAPI shield = ship.getShield();
		if (shield == null) {
			ship.setShield(ShieldType.FRONT, ((ship.getHullSize().ordinal() - 1) * 65) / ship.getMutableStats().getFluxDissipation().base, Efficiency, ShieldArc);
		}
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return ShieldArc + "";
		if (index == 1) {
			switch (hullSize) {
				case FRIGATE:
					return "~100 flux/s";
				case DESTROYER:
					return "~200 flux/s";
				case CRUISER:
					return "~300 flux/s";
				case CAPITAL_SHIP:
					return "~400 flux/s";
				default:
				return null;
			}
		}
		if (index == 2) return Efficiency + "";
        return null;
    }
}
