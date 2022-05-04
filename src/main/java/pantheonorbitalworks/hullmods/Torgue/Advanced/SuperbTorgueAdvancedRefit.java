package pantheonorbitalworks.hullmods.Torgue.Advanced;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class SuperbTorgueAdvancedRefit extends BaseHullMod {

	public float QualityMultiplier = 1.1f;
	public String Quality = "Superb";

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getHullBonus().modifyPercent(id, AdvancedConstants.Hull * QualityMultiplier);
		stats.getArmorBonus().modifyPercent(id, AdvancedConstants.Armor * QualityMultiplier);
		stats.getMaxSpeed().modifyPercent(id, -AdvancedConstants.MaxSpeed);
		stats.getAcceleration().modifyPercent(id, -AdvancedConstants.Acceleration);
		stats.getDeceleration().modifyPercent(id, -AdvancedConstants.Deceleration);
		stats.getMaxTurnRate().modifyPercent(id, -AdvancedConstants.TurnRate);
		stats.getTurnAcceleration().modifyPercent(id, -AdvancedConstants.TurnAcceleration);
		stats.getMinCrewMod().modifyPercent(id, AdvancedConstants.MinCrew);
		stats.getPeakCRDuration().modifyPercent(id, -AdvancedConstants.PeakCR);
		stats.getSuppliesPerMonth().modifyPercent(id, AdvancedConstants.Supplies);
		stats.getSuppliesToRecover().modifyPercent(id, AdvancedConstants.Supplies);
		stats.getTimeMult().modifyMult(id, ((AdvancedConstants.ShipSpeed * QualityMultiplier) / 100) + 1);
		stats.getMissileMaxSpeedBonus().modifyPercent(id, AdvancedConstants.MissileSpeed * QualityMultiplier);
		stats.getMissileAccelerationBonus().modifyPercent(id, AdvancedConstants.MissileSpeed * QualityMultiplier);
		stats.getMissileMaxTurnRateBonus().modifyPercent(id, -AdvancedConstants.MissileTurn);
		stats.getMissileTurnAccelerationBonus().modifyPercent(id, -AdvancedConstants.MissileTurn);
		stats.getMissileWeaponRangeBonus().modifyPercent(id, -AdvancedConstants.MissileTurn);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0)
			return Quality;
		if (index == 1)
			return AdvancedConstants.Hull * QualityMultiplier + "%";
		if (index == 2)
			return AdvancedConstants.Armor * QualityMultiplier + "%";
		if (index == 3)
			return AdvancedConstants.Acceleration + "%";
		if (index == 4)
			return AdvancedConstants.MinCrew + "%";
		if (index == 5)
			return AdvancedConstants.PeakCR + "%";
		if (index == 6)
			return AdvancedConstants.Supplies + "%";
		if (index == 7)
			return AdvancedConstants.ShipSpeed * QualityMultiplier + "%";
		if (index == 8)
			return AdvancedConstants.MissileSpeed * QualityMultiplier + "%";
		if (index == 9)
			return AdvancedConstants.MissileTurn + "%";
		return null;
	}
}
