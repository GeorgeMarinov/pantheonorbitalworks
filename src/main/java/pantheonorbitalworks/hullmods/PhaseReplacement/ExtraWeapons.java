package pantheonorbitalworks.hullmods.PhaseReplacement;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

public class ExtraWeapons extends BaseHullMod {

	public static final float ShieldArc = 120f;
	public static final float Upkeep = 0.2f;
	public static final float Efficiency = 1.2f;

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		for (int i = 0; i < 20; i++) {
			WeaponSlotAPI slot = ship.getVariant().getSlot("WS00" + (99 - i));
			if (slot != null) {
				ship.getVariant().addWeapon("WS00" + (99 - i), "torgue_phase_replacement");
			}
		}
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0)
			return ShieldArc + "";
		if (index == 1)
			return Upkeep * 100 + "%";
		if (index == 2)
			return Efficiency + "";
		return null;
	}
}
