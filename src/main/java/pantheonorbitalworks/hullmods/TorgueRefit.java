package pantheonorbitalworks.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

public class TorgueRefit extends BaseHullMod {

	public static final float Hull = 10;
	public static final float Armor = 10;
	public static final float MaxSpeed = 5;
	public static final float Acceleration = 5;
	public static final float Deceleration = 5;
	public static final float TurnRate = 5;
	public static final float TurnAcceleration = 5;
	public static final float MinCrew = 10;
	public static final float PeakCR = 10;
	public static final float Supplies = 10;
	public static final float ShipSpeed = 5;
	public static final float MissileSpeed = 40;
	public static final float MissileTurn = 20;
	public static final float MissileRangeReduction = 20;
	public String ShipId = "";

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		FleetMemberAPI fleetMember = stats.getFleetMember();
		if (fleetMember != null) {
			float PackageMultiplier = 1.0f;
			float QualityMultiplier = 1.0f;
			String shipId = fleetMember.getId();
			int modIdsIndex = shipId.indexOf("modIds=");
			int shipNameIndex = shipId.indexOf("shipName=");
			if (shipNameIndex != -1) {
				String shipName = shipId.substring(shipNameIndex + 9, modIdsIndex);
				if (fleetMember.getShipName().equals(shipName) && modIdsIndex != -1) {
					ShipId = shipId;

					String[] mods = shipId.substring(modIdsIndex + 7).split(",");
					for (int i = 0; i < mods.length; i++) {
						if (mods[i].contains("superb")) {
							QualityMultiplier = 1.1f;
						}
						if (mods[i].contains("legendary")) {
							QualityMultiplier = 1.3f;
						}
						if (mods[i].contains("masterwork")) {
							QualityMultiplier = 1.5f;
						}
						if (mods[i].contains("miracle")) {
							QualityMultiplier = 2f;
						}
						if (mods[i].contains("advanced")) {
							PackageMultiplier = 2f;
						}
						if (mods[i].contains("expert")) {
							PackageMultiplier = 2.5f;
						}
						if (mods[i].contains("_refit")) {
							stats.getHullBonus().modifyPercent(id, (Hull * PackageMultiplier) * QualityMultiplier);
							stats.getArmorBonus().modifyPercent(id, (Armor * PackageMultiplier) * QualityMultiplier);
							stats.getMaxSpeed().modifyPercent(id, -(MaxSpeed * PackageMultiplier));
							stats.getAcceleration().modifyPercent(id, -(Acceleration * PackageMultiplier));
							stats.getDeceleration().modifyPercent(id, -(Deceleration * PackageMultiplier));
							stats.getMaxTurnRate().modifyPercent(id, -(TurnRate * PackageMultiplier));
							stats.getTurnAcceleration().modifyPercent(id, -(TurnAcceleration * PackageMultiplier));
							stats.getMinCrewMod().modifyPercent(id, (MinCrew * PackageMultiplier));
							stats.getPeakCRDuration().modifyPercent(id, -(PeakCR * PackageMultiplier));
							stats.getSuppliesPerMonth().modifyPercent(id, (Supplies * PackageMultiplier));
							stats.getSuppliesToRecover().modifyPercent(id, (Supplies * PackageMultiplier));
							stats.getTimeMult().modifyMult(id,
									(((ShipSpeed * PackageMultiplier) * QualityMultiplier) / 100) + 1);
							stats.getMissileMaxSpeedBonus().modifyPercent(id,
									(MissileSpeed * PackageMultiplier) * QualityMultiplier);
							stats.getMissileAccelerationBonus().modifyPercent(id,
									(MissileSpeed * PackageMultiplier) * QualityMultiplier);
							stats.getMissileMaxTurnRateBonus().modifyPercent(id, -(MissileTurn * PackageMultiplier));
							stats.getMissileTurnAccelerationBonus().modifyPercent(id,
									-(MissileTurn * PackageMultiplier));
							stats.getMissileWeaponRangeBonus().modifyPercent(id,
									-(MissileRangeReduction * PackageMultiplier));
						}
						if (mods[i].contains("extra_cargo")) {
							stats.getCargoMod().modifyPercent(id, 150);
						}
						if (mods[i].contains("extra_flux_capacity_and_dissipation")) {
							stats.getFluxCapacity().modifyPercent(id, 30);
							stats.getFluxDissipation().modifyPercent(id, 30);
						}
					}
				}
			}
		}
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		int modIdsIndex = ShipId.indexOf("modIds=");
		if (modIdsIndex != -1) {
			String[] mods = ShipId.substring(modIdsIndex + 7).split(",");
			for (int i = 0; i < mods.length; i++) {
				if (mods[i].contains("extra_weapons")) {
					for (int y = 0; y < 20; y++) {
						WeaponSlotAPI slot = ship.getVariant().getSlot("WS00" + (99 - y));
						if (slot != null) {
							ship.getVariant().addWeapon("WS00" + (99 - y), "torgue_phase_replacement");
						}
					}
				}
				if (mods[i].contains("low_tech_shield_emitter")) {
					ShieldAPI shield = ship.getShield();
					if (shield == null) {
						ship.setShield(ShieldType.OMNI, ((ship.getHullSize().ordinal() - 1) * 100)
								/ ship.getMutableStats().getFluxDissipation().base, 1.1f, 120f);
					}
				}
				if (mods[i].contains("mid_tech_shield_emitter")) {
					ShieldAPI shield = ship.getShield();
					if (shield == null) {
						ship.setShield(ShieldType.OMNI, ((ship.getHullSize().ordinal() - 1) * 100)
								/ ship.getMutableStats().getFluxDissipation().base, 1f, 210f);
					}
				}
				if (mods[i].contains("high_tech_shield_emitter")) {
					ShieldAPI shield = ship.getShield();
					if (shield == null) {
						ship.setShield(ShieldType.OMNI, ((ship.getHullSize().ordinal() - 1) * 100)
								/ ship.getMutableStats().getFluxDissipation().base, 0.9f, 180f);
					}
				}
				if (mods[i].contains("pow_low_tech_shield")) {
					ship.setShield(ShieldType.FRONT, ((ship.getHullSize().ordinal() - 1) * 65)
							/ ship.getMutableStats().getFluxDissipation().base, 1.1f, 120f);
					ship.getMutableStats().getSuppliesPerMonth().modifyPercent(id, -10);
					ship.getMutableStats().getSuppliesToRecover().modifyPercent(id, -10);
				}
				if (mods[i].contains("pow_mid_tech_shield")) {
					ship.setShield(ShieldType.FRONT, ((ship.getHullSize().ordinal() - 1) * 65)
							/ ship.getMutableStats().getFluxDissipation().base, 1f, 210f);
					ship.getMutableStats().getSuppliesPerMonth().modifyPercent(id, -Supplies);
					ship.getMutableStats().getSuppliesToRecover().modifyPercent(id, -Supplies);
				}
				if (mods[i].contains("pow_high_tech_shield")) {
					ship.setShield(ShieldType.OMNI, ((ship.getHullSize().ordinal() - 1) * 100)
							/ ship.getMutableStats().getFluxDissipation().base, 0.9f, 180f);
					ship.getMutableStats().getSuppliesPerMonth().modifyPercent(id, -Supplies);
					ship.getMutableStats().getSuppliesToRecover().modifyPercent(id, -Supplies);
				}
			}
		}

	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		if (ShipId.contains("miracle")) {
			ship.setJitterUnder(ship, new Color(255, 255, 100, 255), 0.5f, 5, 100);
		}
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		FleetMemberAPI someMember = ship.getMutableStats().getFleetMember();
		if (someMember != null) {
			String copium = someMember.getId();
			float PackageMultiplier = 1.0f;
			float QualityMultiplier = 1.0f;
			String quality = "normal";
			if (copium.contains("superb")) {
				QualityMultiplier = 1.1f;
				quality = "Superb";
			}
			if (copium.contains("legendary")) {
				QualityMultiplier = 1.3f;
				quality = "Legendary";
			}
			if (copium.contains("masterwork")) {
				QualityMultiplier = 1.5f;
				quality = "Masterwork";
			}
			if (copium.contains("miracle")) {
				QualityMultiplier = 2f;
				quality = "Miracle";
			}
			if (copium.contains("advanced")) {
				PackageMultiplier = 2f;
			}
			if (copium.contains("expert")) {
				PackageMultiplier = 2.5f;
			}
			if (index == 0)
				return quality;
			if (index == 1)
				return (Hull * PackageMultiplier) * QualityMultiplier + "%";
			if (index == 2)
				return (Armor * PackageMultiplier) * QualityMultiplier + "%";
			if (index == 3)
				return (Acceleration * PackageMultiplier) + "%";
			if (index == 4)
				return (MinCrew * PackageMultiplier) + "%";
			if (index == 5)
				return (PeakCR * PackageMultiplier) + "%";
			if (index == 6)
				return (Supplies * PackageMultiplier) + "%";
			if (index == 7)
				return (ShipSpeed * PackageMultiplier) * QualityMultiplier + "%";
			if (index == 8)
				return (MissileSpeed * PackageMultiplier) * QualityMultiplier + "%";
			if (index == 9)
				return (MissileTurn * PackageMultiplier) + "%";
		}

		return null;
	}
}