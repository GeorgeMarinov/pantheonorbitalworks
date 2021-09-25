package pantheonorbitalworks.hullmods.Shields;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class LowTechShield extends BaseHullMod {

	public static final float ShieldArc = 120f;
	public static final float Efficiency = 1.1f;
	public static final int Supplies = 10;

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.setShield(ShieldType.FRONT, ((ship.getHullSize().ordinal() - 1) * 30) / ship.getMutableStats().getFluxDissipation().base, Efficiency, ShieldArc);
			ship.getMutableStats().getSuppliesPerMonth().modifyPercent(id, -Supplies);
			ship.getMutableStats().getSuppliesToRecover().modifyPercent(id, -Supplies);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return ShieldArc + "";
		if (index == 1) {
			switch (hullSize) {
				case FRIGATE:
					return "~30 flux/s";
				case DESTROYER:
					return "~60 flux/s";
				case CRUISER:
					return "~90 flux/s";
				case CAPITAL_SHIP:
					return "~120 flux/s";
				default:
				return null;
			}
		}
		if (index == 2) return Efficiency + "";
		if (index == 2) return Supplies + "%";
        return null;
    }
}
