package pantheonorbitalworks.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import pantheonorbitalworks.RefitRepresentative;
import pantheonorbitalworks.RefitableShips;

public class SpawnWorldRefits implements CampaignEventListener {
    public static final String[] hullMods = new String[] {
            "eccm", "augmentedengines", "autorepair", "auxiliarythrusters",
            "blast_doors", "ecm", "efficiency_overhaul", "fluxbreakers",
            "fluxcoil", "fluxdistributor", "hardened_subsystems", "hardenedshieldemitter",
            "heavyarmor", "insulatedengine", "nav_relay", "reinforcedhull",
            "solar_shielding", "stabilizedshieldemitter"
    };

    @Override
    public void reportEconomyMonthEnd() {

    }

    @Override
    public void reportPlayerOpenedMarket(MarketAPI market) {

    }

    @Override
    public void reportPlayerClosedMarket(MarketAPI market) {

    }

    @Override
    public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {

    }

    @Override
    public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {

    }

    @Override
    public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {

    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle) {

    }

    @Override
    public void reportBattleFinished(CampaignFleetAPI primaryWinner, BattleAPI battle) {

    }

    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {

    }

    @Override
    public void reportFleetDespawned(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {

    }

    @Override
    public void reportFleetSpawned(CampaignFleetAPI fleet) {
        if (!fleet.isPlayerFleet()) {
            Random random = new Random();
            FleetDataAPI fleetData = fleet.getFleetData();
            int baseChance = Math.round(fleet.getEffectiveStrength() / 100f) * 5;
            int upgradeHullChance = baseChance * 2;

            for (FleetMemberAPI fleetMember : fleetData.getMembersListCopy()) {
                if (random.nextInt(101) < baseChance) {
                    for (RefitableShips refittableShip : RefitableShips.values()) {
                        if (fleetMember.getHullId().equals(refittableShip.toString())) {
                            RefitRepresentative refitRepresentative = RefitRepresentative.list[(random
                                    .nextInt(RefitRepresentative.list.length))];
                            String manufacturer = refitRepresentative.Make.toLowerCase();
                            String chosenPackage = "basic";
                            String hullMod = "";
                            if (random.nextInt(101) < upgradeHullChance) {
                                chosenPackage = "advanced";
                                if (random.nextInt(101) < upgradeHullChance) {
                                    chosenPackage = "expert";
                                }
                            }

                            int qualityChance = random.nextInt(101);
                            if (qualityChance < 50) {
                                hullMod = "normal_" + manufacturer + "_" + chosenPackage + "_refit";
                            } else if (qualityChance < 70) {
                                hullMod = "superb_" + manufacturer + "_" + chosenPackage + "_refit";
                            } else if (qualityChance < 80) {
                                hullMod = "legendary_" + manufacturer + "_" + chosenPackage + "_refit";
                            } else if (qualityChance < 90) {
                                hullMod = "masterwork_" + manufacturer + "_" + chosenPackage + "_refit";
                            } else if (qualityChance < 98) {
                                hullMod = "miracle_" + manufacturer + "_" + chosenPackage + "_refit";
                            } else {
                                hullMod = "normal_" + manufacturer + "_" + chosenPackage + "_refit";
                            }

                            float currentCR = fleetMember.getRepairTracker().getCR();
                            PersonAPI pilot = fleetMember.getCaptain();
                            fleetData.removeFleetMember(fleetMember);
                            FleetMemberAPI newShip = Global.getFactory().createFleetMember(FleetMemberType.SHIP,
                                    fleetMember.isPhaseShip()
                                            ? manufacturer + "_" + chosenPackage + "_phase_" + fleetMember.getHullId()
                                                    + "_Standard"
                                            : manufacturer + "_" + chosenPackage + "_" + fleetMember.getHullId()
                                                    + "_Standard");

                            newShip.setId(newShip.getId() + "modIds=" + hullMod);
                            fleetData.addFleetMember(newShip);
                            newShip.getRepairTracker().setCR(currentCR);
                            newShip.setCaptain(pilot);

                            // log hull id error
                            if (newShip.getHullId().contains("nebula")) {
                                Global.getSector().getCampaignUI().addMessage(manufacturer + "_" + chosenPackage + "_"
                                        + fleetMember.getHullId() + "_Standard");
                            }

                            List<String> modList = new ArrayList<String>(Arrays.asList(hullMods));
                            while (random.nextInt(101) < baseChance && !modList.isEmpty()) {
                                ShipVariantAPI variant = newShip.getVariant();
                                Collection<String> shipMods = variant.getNonBuiltInHullmods();
                                for (String mod : shipMods) {
                                    if (modList.contains(mod)) {
                                        modList.remove(mod);
                                    }
                                }
                                String chosenMod = modList.get(random.nextInt(modList.size()));
                                variant.addPermaMod(chosenMod, true);

                                int indexOfWildMods = newShip.getId().indexOf("wildMods=");
                                if (indexOfWildMods != -1) {
                                    String currentWildMods = newShip.getId().substring(indexOfWildMods);
                                    newShip.setId(newShip.getId().replace(currentWildMods,
                                            currentWildMods + "," + chosenMod));
                                } else {
                                    newShip.setId(newShip.getId() + "wildMods=" + chosenMod);
                                }
                            }

                            break;
                        }
                    }
                }
            }

            fleetData.sort();
        }
    }

    @Override
    public void reportFleetReachedEntity(CampaignFleetAPI fleet, SectorEntityToken entity) {

    }

    @Override
    public void reportFleetJumped(CampaignFleetAPI fleet, SectorEntityToken from, JumpPointAPI.JumpDestination to) {

    }

    @Override
    public void reportShownInteractionDialog(InteractionDialogAPI dialog) {

    }

    @Override
    public void reportPlayerReputationChange(String faction, float delta) {

    }

    @Override
    public void reportPlayerReputationChange(PersonAPI person, float delta) {

    }

    @Override
    public void reportPlayerActivatedAbility(AbilityPlugin ability, Object param) {

    }

    @Override
    public void reportPlayerDeactivatedAbility(AbilityPlugin ability, Object param) {

    }

    @Override
    public void reportPlayerDumpedCargo(CargoAPI cargo) {

    }

    @Override
    public void reportPlayerDidNotTakeCargo(CargoAPI cargo) {

    }

    @Override
    public void reportEconomyTick(int iterIndex) {

    }
}