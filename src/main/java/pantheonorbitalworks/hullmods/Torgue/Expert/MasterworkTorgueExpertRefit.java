package pantheonorbitalworks.hullmods.Torgue.Expert;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MasterworkTorgueExpertRefit extends BaseHullMod {

	public float QualityMultiplier = 1.5f;
	public String Quality = "Masterwork";
	public int Hull = Math.round(400 * QualityMultiplier);
	public int Armor = Math.round(150 * QualityMultiplier);
	public int Acceleration = Math.round(40 * QualityMultiplier);
	public int Deceleration = Math.round(10 * QualityMultiplier);
	public float ShipSpeed = 15 * QualityMultiplier;
    public int MissileSpeed = Math.round(100 * QualityMultiplier);
	public int MissileTurn = 50;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSuppliesPerMonth().modifyMult(id, 1.60f);
		stats.getHullBonus().modifyFlat(id, Hull);
		stats.getArmorBonus().modifyFlat(id, Armor);
		stats.getAcceleration().modifyFlat(id, Acceleration);
		stats.getDeceleration().modifyFlat(id, Deceleration);
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
		if (index == 4) return Deceleration + "";
        if (index == 5) return ShipSpeed + "%";
        if (index == 6) return ShipSpeed + "%";
        if (index == 7) return MissileSpeed + "%";
        if (index == 8) return MissileTurn + "%";
        return null;
    }
}
