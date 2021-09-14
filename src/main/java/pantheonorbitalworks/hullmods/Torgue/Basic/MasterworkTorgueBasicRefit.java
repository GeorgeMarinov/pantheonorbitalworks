package pantheonorbitalworks.hullmods.Torgue.Basic;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MasterworkTorgueBasicRefit extends BaseHullMod {

	public float QualityMultiplier = 1.5f;
	public String Quality = "Masterwork";
	public int Hull = Math.round(200 * QualityMultiplier);
	public int Armor = Math.round(50 * QualityMultiplier);
	public int Acceleration = Math.round(20 * QualityMultiplier);
	public float ShipSpeed = 5 * QualityMultiplier;
    public int MissileSpeed = Math.round(20 * QualityMultiplier);
	public int MissileTurn = 20;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSuppliesPerMonth().modifyMult(id, 1.1f);
		stats.getHullBonus().modifyFlat(id, Hull);
		stats.getArmorBonus().modifyFlat(id, Armor);
		stats.getAcceleration().modifyFlat(id, Acceleration);
		stats.getTimeMult().modifyMult(id, (ShipSpeed / 100) + 1);
		stats.getMissileMaxSpeedBonus().modifyPercent(id, MissileSpeed);
		stats.getMissileAccelerationBonus().modifyPercent(id, MissileSpeed);
		stats.getMissileMaxTurnRateBonus().modifyPercent(id, -MissileTurn);
		stats.getMissileTurnAccelerationBonus().modifyPercent(id, -MissileTurn);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return Quality;
		if (index == 1) return Hull + "";
		if (index == 2) return Armor + "";
		if (index == 3) return Acceleration + "";
        if (index == 4) return ShipSpeed + "%";
        if (index == 5) return ShipSpeed + "%";
        if (index == 6) return MissileSpeed + "%";
        if (index == 7) return MissileTurn + "%";
        return null;
    }
}
