package pantheonorbitalworks.hullmods.Torgue.Basic;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class SuperbTorgueBasicRefit extends BaseHullMod {

	public float QualityMultiplier = 1.1f;
	public String Quality = "Superb";

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getHullBonus().modifyPercent(id, BasicConstants.Hull * QualityMultiplier);
		stats.getArmorBonus().modifyPercent(id, BasicConstants.Armor * QualityMultiplier);
		stats.getMaxSpeed().modifyPercent(id, -BasicConstants.MaxSpeed);
		stats.getAcceleration().modifyPercent(id, -BasicConstants.Acceleration);
		stats.getDeceleration().modifyPercent(id, -BasicConstants.Deceleration);
		stats.getMaxTurnRate().modifyPercent(id, -BasicConstants.TurnRate);
		stats.getTurnAcceleration().modifyPercent(id, -BasicConstants.TurnAcceleration);
		stats.getMinCrewMod().modifyPercent(id, BasicConstants.MinCrew);
		stats.getPeakCRDuration().modifyPercent(id, -BasicConstants.PeakCR);
		stats.getSuppliesPerMonth().modifyPercent(id, BasicConstants.Supplies);
		stats.getSuppliesToRecover().modifyPercent(id, BasicConstants.Supplies);
		stats.getTimeMult().modifyMult(id, ((BasicConstants.ShipSpeed * QualityMultiplier) / 100) + 1);
		stats.getMissileMaxSpeedBonus().modifyPercent(id, BasicConstants.MissileSpeed * QualityMultiplier);
		stats.getMissileAccelerationBonus().modifyPercent(id, BasicConstants.MissileSpeed * QualityMultiplier);
		stats.getMissileMaxTurnRateBonus().modifyPercent(id, -BasicConstants.MissileTurn);
		stats.getMissileTurnAccelerationBonus().modifyPercent(id, -BasicConstants.MissileTurn);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0)
			return Quality;
		if (index == 1)
			return BasicConstants.Hull * QualityMultiplier + "%";
		if (index == 2)
			return BasicConstants.Armor * QualityMultiplier + "%";
		if (index == 3)
			return BasicConstants.Acceleration + "%";
		if (index == 4)
			return BasicConstants.MinCrew + "%";
		if (index == 5)
			return BasicConstants.PeakCR + "%";
		if (index == 6)
			return BasicConstants.Supplies + "%";
		if (index == 7)
			return BasicConstants.ShipSpeed * QualityMultiplier + "%";
		if (index == 8)
			return BasicConstants.MissileSpeed * QualityMultiplier + "%";
		if (index == 9)
			return BasicConstants.MissileTurn + "%";
		return null;
	}
}
