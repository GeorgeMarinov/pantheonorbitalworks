package pantheonorbitalworks.hullmods.Shields;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HighTechShield extends BaseHullMod {

	public static final float ShieldArc = 180f;
	public static final float Efficiency = 0.9f;
	public static final int Supplies = 30;

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.setShield(ShieldType.OMNI, ((ship.getHullSize().ordinal() - 1) * 100) / ship.getMutableStats().getFluxDissipation().base, Efficiency, ShieldArc);
		// ShieldAPI shield = ship.getShield();
		// if (shield == null) {
			
		// } else {
		// 	shield.setArc(ShieldArc);
		// 	shield.setType(ShieldType.OMNI);
		// 	ship.getMutableStats().getShieldUpkeepMult().
		// }

		ship.getMutableStats().getSuppliesPerMonth().modifyPercent(id, -Supplies);
		ship.getMutableStats().getSuppliesToRecover().modifyPercent(id, -Supplies);
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
					return "~305 flux/s";
				case CAPITAL_SHIP:
					return "~400 flux/s";
				default:
				return null;
			}
		}
		if (index == 2) return Efficiency + "";
		if (index == 2) return Supplies + "%";
        return null;
    }
}
