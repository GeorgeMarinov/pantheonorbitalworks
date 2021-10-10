package pantheonorbitalworks.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class POW_WeaponFocusStats extends BaseShipSystemScript {

	public static final float DAMAGE_BONUS_PERCENT = 30f;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		stats.getEnergyWeaponDamageMult().modifyPercent(id, bonusPercent);
		stats.getMissileWeaponDamageMult().modifyPercent(id, bonusPercent);
		stats.getBallisticWeaponDamageMult().modifyPercent(id, bonusPercent);
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().unmodify(id);
		stats.getMissileWeaponDamageMult().unmodify(id);
		stats.getBallisticWeaponDamageMult().unmodify(id);
	}

	public StatusData getStatusData(int index, State state, float effectLevel) {
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		if (index == 0) {
			return new StatusData("+" + (int) bonusPercent + "% weapon damage", false);
		}
		return null;
	}
}
