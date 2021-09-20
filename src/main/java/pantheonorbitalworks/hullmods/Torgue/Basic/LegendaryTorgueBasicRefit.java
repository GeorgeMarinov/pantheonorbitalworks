package pantheonorbitalworks.hullmods.Torgue.Basic;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class LegendaryTorgueBasicRefit extends BaseHullMod {

	public float QualityMultiplier = 1.3f;
	public String Quality = "Legendary";
	public float Hull = 10 * QualityMultiplier;
	public float Armor = 10 * QualityMultiplier;
	public int MaxSpeed = 10;
	public int Acceleration = 10;
	public int Deceleration = 10;
	public int TurnRate = 10;
	public int TurnAcceleration = 10;
	public int MinCrew = 15;
	public int PeakCR = 30;
	public int Supplies = 20;
	public float ShipSpeed = 5 * QualityMultiplier;
    public float MissileSpeed = 20 * QualityMultiplier;
	public int MissileTurn = 20;

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
		stats.getMissileWeaponRangeBonus().modifyPercent(id, -10);
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
