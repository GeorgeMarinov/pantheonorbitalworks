package pantheonorbitalworks.hullmods.Shields;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MidTechShield extends BaseHullMod {

	public static final float ShieldArc = 210f;
	public static final float Efficiency = 1.0f;
	public static final int Supplies = 20;

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.setShield(ShieldType.FRONT, ((ship.getHullSize().ordinal() - 1) * 65) / ship.getMutableStats().getFluxDissipation().base, Efficiency, ShieldArc);
			ship.getMutableStats().getSuppliesPerMonth().modifyPercent(id, -Supplies);
			ship.getMutableStats().getSuppliesToRecover().modifyPercent(id, -Supplies);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return ShieldArc + "";
		if (index == 1) {
			switch (hullSize) {
				case FRIGATE:
					return "~65 flux/s";
				case DESTROYER:
					return "~130 flux/s";
				case CRUISER:
					return "~195 flux/s";
				case CAPITAL_SHIP:
					return "~260 flux/s";
				default:
				return null;
			}
		}
		if (index == 2) return Efficiency + "";
		if (index == 2) return Supplies + "%";
        return null;
    }
}
