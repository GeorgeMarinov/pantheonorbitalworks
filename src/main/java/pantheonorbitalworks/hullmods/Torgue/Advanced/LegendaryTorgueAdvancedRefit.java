package pantheonorbitalworks.hullmods.Torgue.Advanced;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class LegendaryTorgueAdvancedRefit extends BaseHullMod {

	public float QualityMultiplier = 1.3f;
	public String Quality = "Legendary";
	public float Hull = 15 * QualityMultiplier;
	public float Armor = 20 * QualityMultiplier;
	public int MaxSpeed = 15;
	public int Acceleration = 15;
	public int Deceleration = 15;
	public int TurnRate = 15;
	public int TurnAcceleration = 15;
	public int MinCrew = 20;
	public int PeakCR = 30;
	public int Supplies = 40;
	public float ShipSpeed = 10 * QualityMultiplier;
    public float MissileSpeed = 50 * QualityMultiplier;
	public int MissileTurn = 30;

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
		stats.getMissileWeaponRangeBonus().modifyPercent(id, -20);
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
