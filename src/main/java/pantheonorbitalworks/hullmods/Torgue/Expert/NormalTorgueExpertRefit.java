package pantheonorbitalworks.hullmods.Torgue.Expert;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class NormalTorgueExpertRefit extends BaseHullMod {

	public float QualityMultiplier = 1.0f;
	public String Quality = "Normal";

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getHullBonus().modifyPercent(id, ExpertConstants.Hull * QualityMultiplier);
		stats.getArmorBonus().modifyPercent(id, ExpertConstants.Armor * QualityMultiplier);
		stats.getMaxSpeed().modifyPercent(id, -ExpertConstants.MaxSpeed);
		stats.getAcceleration().modifyPercent(id, -ExpertConstants.Acceleration);
		stats.getDeceleration().modifyPercent(id, -ExpertConstants.Deceleration);
		stats.getMaxTurnRate().modifyPercent(id, -ExpertConstants.TurnRate);
		stats.getTurnAcceleration().modifyPercent(id, -ExpertConstants.TurnAcceleration);
		stats.getMinCrewMod().modifyPercent(id, ExpertConstants.MinCrew);
		stats.getPeakCRDuration().modifyPercent(id, -ExpertConstants.PeakCR);
		stats.getSuppliesPerMonth().modifyPercent(id, ExpertConstants.Supplies);
		stats.getSuppliesToRecover().modifyPercent(id, ExpertConstants.Supplies);
		stats.getTimeMult().modifyMult(id, ((ExpertConstants.ShipSpeed * QualityMultiplier) / 100) + 1);
		stats.getMissileMaxSpeedBonus().modifyPercent(id, ExpertConstants.MissileSpeed * QualityMultiplier);
		stats.getMissileAccelerationBonus().modifyPercent(id, ExpertConstants.MissileSpeed * QualityMultiplier);
		stats.getMissileMaxTurnRateBonus().modifyPercent(id, -ExpertConstants.MissileTurn);
		stats.getMissileTurnAccelerationBonus().modifyPercent(id, -ExpertConstants.MissileTurn);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0)
			return Quality;
		if (index == 1)
			return ExpertConstants.Hull * QualityMultiplier + "%";
		if (index == 2)
			return ExpertConstants.Armor * QualityMultiplier + "%";
		if (index == 3)
			return ExpertConstants.Acceleration + "%";
		if (index == 4)
			return ExpertConstants.MinCrew + "%";
		if (index == 5)
			return ExpertConstants.PeakCR + "%";
		if (index == 6)
			return ExpertConstants.Supplies + "%";
		if (index == 7)
			return ExpertConstants.ShipSpeed * QualityMultiplier + "%";
		if (index == 8)
			return ExpertConstants.MissileSpeed * QualityMultiplier + "%";
		if (index == 9)
			return ExpertConstants.MissileTurn + "%";
		return null;
	}
}
