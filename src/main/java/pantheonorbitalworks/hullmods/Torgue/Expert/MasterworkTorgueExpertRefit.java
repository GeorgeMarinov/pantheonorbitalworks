package pantheonorbitalworks.hullmods.Torgue.Expert;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MasterworkTorgueExpertRefit extends BaseHullMod {

	public float QualityMultiplier = 1.5f;
	public String Quality = "Masterwork";
	public float Hull = 20 * QualityMultiplier;
	public float Armor = 30 * QualityMultiplier;
	public int MaxSpeed = 20;
	public int Acceleration = 20;
	public int Deceleration = 20;
	public int TurnRate = 20;
	public int TurnAcceleration = 20;
	public int MinCrew = 25;
	public int PeakCR = 30;
	public int Supplies = 70;
	public float ShipSpeed = 15 * QualityMultiplier;
    public float MissileSpeed = 100 * QualityMultiplier;
	public int MissileTurn = 50;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getHullBonus().modifyPercent(id, Hull);
		stats.getArmorBonus().modifyPercent(id, Armor);
		stats.getMaxSpeed().modifyPercent(id, -MaxSpeed);
		stats.getAcceleration().modifyPercent(id, -Acceleration);
		stats.getDeceleration().modifyPercent(id, -Deceleration);
		stats.getMaxTurnRate().modifyPercent(id, -TurnRate);
		stats.getTurnAcceleration().modifyPercent(id, -TurnAcceleration);
		stats.getMinCrewMod().modifyPercent(id, MinCrew);
		stats.getPeakCRDuration().modifyPercent(id, -PeakCR);
		stats.getSuppliesPerMonth().modifyPercent(id, Supplies);
		stats.getSuppliesToRecover().modifyPercent(id, Supplies);
		stats.getTimeMult().modifyMult(id, (ShipSpeed / 100) + 1);
		stats.getMissileWeaponRangeBonus().modifyPercent(id, -30);
		stats.getMissileMaxSpeedBonus().modifyPercent(id, MissileSpeed);
		stats.getMissileAccelerationBonus().modifyPercent(id, MissileSpeed);
		stats.getMissileMaxTurnRateBonus().modifyPercent(id, -MissileTurn);
		stats.getMissileTurnAccelerationBonus().modifyPercent(id, -MissileTurn);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return Quality;
		if (index == 1) return Hull + "%";
		if (index == 2) return Armor + "%";
		if (index == 3) return Acceleration + "%";
		if (index == 4) return MinCrew + "%";
		if (index == 5) return PeakCR + "%";
		if (index == 6) return Supplies + "%";
        if (index == 7) return ShipSpeed + "%";
        if (index == 8) return MissileSpeed + "%";
        if (index == 9) return MissileTurn + "%";
        return null;
    }
}
