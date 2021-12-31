package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc.Token;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pantheonorbitalworks.RefitPackage;
import pantheonorbitalworks.RefitRepresentative;
import pantheonorbitalworks.RefitShip;
import pantheonorbitalworks.RefitableCapital;
import pantheonorbitalworks.RefitableCruiser;
import pantheonorbitalworks.RefitableDestroyer;
import pantheonorbitalworks.RefitableFrigade;
import pantheonorbitalworks.RefitablePhase;

public class CEO extends PaginatedOptions {

	public final RefitRepresentative[] refitRepresentative = new RefitRepresentative[] {
			new RefitRepresentative("Mr. Torgue", "Torgue"), new RefitRepresentative("Andreyevna", "Vladof"),
			new RefitRepresentative("Katagawa Jr.", "Maliwan"), new RefitRepresentative("Jacobs", "Jacobs"),
			new RefitRepresentative("Rhys Strongfork", "Atlas"), new RefitRepresentative("Handsome Jack", "Hyperion") };

	public enum DialogIdKeys {
		chosenHullId, originalHullPackage, isUpgrade, creditsCost, newPackage, newHullConfirmed, chosenShipName,
		originalHullId, isPhase, replacePhaseCoils, finalMenuState, newShield, chosenShipSize
	}

	public enum FinalMenuStates {
		preview, phase_coils, shield_swap
	}

	public enum PhaseCoilReplacemnts {
		extra_cargo, extra_flux_capacity_and_dissipation, low_tech_shield_emitter, mid_tech_shield_emitter,
		high_tech_shield_emitter, extra_weapons
	}

	public enum Shields {
		low_tech_shield, mid_tech_shield, high_tech_shield
	}

	public enum ShipSize {
		frigate, destroyer, cruiser, capital
	}

	protected CampaignFleetAPI playerFleet;
	protected FleetDataAPI fleetData;
	protected List<FleetMemberAPI> fleetList;
	protected List<String> fleetHullIds = new ArrayList<>();
	protected SectorEntityToken entity;
	protected MarketAPI market;
	// protected TextPanelAPI text;
	// protected CargoAPI playerCargo;
	protected PersonAPI person;
	protected FactionAPI faction;
	protected float points;
	protected List<String> disabledOpts = new ArrayList<>();
	protected OptionPanelAPI optionPanel;
	protected VisualPanelAPI visual;
	private Logger log = Global.getLogger(this.getClass());

	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
			Map<String, MemoryAPI> memoryMap) {
		String arg = params.get(0).getString(memoryMap);
		this.dialog = dialog;
		this.memoryMap = memoryMap;

		entity = dialog.getInteractionTarget();
		market = entity.getMarket();
		// text = dialog.getTextPanel();
		playerFleet = Global.getSector().getPlayerFleet();
		fleetData = playerFleet.getFleetData();
		fleetList = fleetData.getMembersListCopy();
		// playerCargo = playerFleet.getCargo();
		person = dialog.getInteractionTarget().getActivePerson();
		optionPanel = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		for (FleetMemberAPI ship : fleetList) {
			fleetHullIds.add(ship.getHullId());
		}

		switch (arg) {
			case "init":
				visual.fadeVisualOut();
				break;
			case "isRefitRepresentative":
				SectorEntityToken sectorEntity = dialog.getInteractionTarget();
				if (sectorEntity != null) {
					PersonAPI person = sectorEntity.getActivePerson();
					if (person != null) {
						for (int i = 0; i < refitRepresentative.length; i++) {
							if (person.getName().getFirst().equals(refitRepresentative[i].Name)) {
								return true;
							}
						}
					}
				} else {
					return false;
				}
				break;
			case "chooseHullSize":
				originalPlugin = dialog.getPlugin();

				dialog.setPlugin(this);
				init(dialog);

				visual.fadeVisualOut();
				optionPanel.clearOptions();

				for (ShipSize shipSize : ShipSize.values()) {
					optionPanel.addOption("Refit " + shipSize + "s",
							DialogIdKeys.chosenShipSize + ":" + shipSize + ";");
				}

				optionPanel.addOption("Exit", "CEO_Menu_Exit");
				optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false,
						false);
				break;
			case "chooseHullType":
				visual.fadeVisualOut();
				optionPanel.clearOptions();
				String option = memoryMap.get(MemKeys.LOCAL).getString("$option");
				Global.getSector().getCampaignUI().addMessage(option);
				if (option.contains(DialogIdKeys.chosenShipSize.toString())) {
					HashMap<String, String> dialogData = parseDialogOptionId(option);
					String chosenShipSizeString = dialogData.get(DialogIdKeys.chosenShipSize.toString());
					if (chosenShipSizeString != null) {
						ShipSize chosenShipSize = ShipSize.valueOf(chosenShipSizeString);
						switch (chosenShipSize) {
							case frigate:
								for (RefitableFrigade refitableShip : RefitableFrigade.values()) {
									displayAvailableHulls(refitableShip.toString());
								}
								break;
							case destroyer:
								for (RefitableDestroyer refitableShip : RefitableDestroyer.values()) {
									displayAvailableHulls(refitableShip.toString());
								}
								break;
							case cruiser:
								for (RefitableCruiser refitableShip : RefitableCruiser.values()) {
									displayAvailableHulls(refitableShip.toString());
								}
								break;
							case capital:
								for (RefitableCapital refitableShip : RefitableCapital.values()) {
									displayAvailableHulls(refitableShip.toString());
								}
								break;
							default:
								break;
						}
					}
				}

				optionPanel.addOption("Exit", "CEO_Menu_Exit");
				optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false,
						false);
				break;
			case "chooseShipToRefit":
				String chosenHull = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (chosenHull.contains(DialogIdKeys.chosenHullId.toString())
						&& !chosenHull.contains(DialogIdKeys.chosenShipName.toString())) {
					visual.fadeVisualOut();
					optionPanel.clearOptions();
					HashMap<String, String> dialogData = parseDialogOptionId(chosenHull);
					String chosenHullId = dialogData.get(DialogIdKeys.chosenHullId.toString());
					for (FleetMemberAPI fleetShip : fleetList) {
						String fleetHullId = fleetShip.getHullId();
						String optionName = "Refit " + fleetShip.getShipName() + ", "
								+ capitalize(fleetHullId.replaceAll("_", " "));
						String optionId = chosenHull;
						if ((fleetHullId.contains(RefitPackage.basic.toString())
								|| fleetHullId.contains(RefitPackage.advanced.toString()))
								&& fleetHullId.substring(fleetHullId.lastIndexOf("_") + 1)
										.equalsIgnoreCase(chosenHullId)) {
							RefitPackage originalHullPackage = getPackageFromHullId(fleetHullId);
							optionId = optionId + DialogIdKeys.isUpgrade + ":true;" + DialogIdKeys.originalHullPackage
									+ ":" + originalHullPackage + ";" + DialogIdKeys.chosenShipName + ":"
									+ fleetShip.getShipName() + ";" + DialogIdKeys.originalHullId + ":"
									+ fleetShip.getHullId() + ";";
							optionPanel.addOption(optionName, optionId);
						}
						if (fleetHullId.equals(chosenHullId)) {
							optionId = optionId + DialogIdKeys.chosenShipName + ":" + fleetShip.getShipName() + ";"
									+ DialogIdKeys.originalHullId + ":" + fleetShip.getHullId() + ";";
							optionPanel.addOption(optionName, optionId);
						}
					}
					optionPanel.addOption("Exit", "CEO_Menu_Exit");
					optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false,
							false);
				}
				break;
			case "isRefitOptionSelected":
				String selectedOption = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (selectedOption.contains(DialogIdKeys.chosenShipName.toString())
						&& !selectedOption.contains(DialogIdKeys.creditsCost.toString())) {
					HashMap<String, String> dialogData = parseDialogOptionId(selectedOption);
					String chosenHullId = dialogData.get(DialogIdKeys.chosenHullId.toString());
					String originalHullId = dialogData.get(DialogIdKeys.originalHullId.toString());
					visual.fadeVisualOut();
					optionPanel.clearOptions();
					for (RefitPackage refitPackage : RefitPackage.values()) {
						int creditsCost = getCreditsCost(originalHullId,
								Boolean.parseBoolean(dialogData.get(DialogIdKeys.isPhase.toString()))
										? "phase_" + chosenHullId
										: chosenHullId,
								refitPackage);
						String optionName = capitalize(chosenHullId) + " " + refitPackage + " package - " + creditsCost
								+ " credits";
						String optionId = selectedOption + DialogIdKeys.creditsCost + ":" + creditsCost + ";"
								+ DialogIdKeys.newPackage + ":" + refitPackage + ";" + DialogIdKeys.finalMenuState + ":"
								+ FinalMenuStates.preview + ";";
						optionPanel.addOption(optionName, optionId);
						if (creditsCost == 0 || playerFleet.getCargo().getCredits().get() < creditsCost) {
							optionPanel.setEnabled(optionId, false);
						}
					}
					optionPanel.addOption("Exit", "CEO_Menu_Exit");
					optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false,
							false);
					return true;
				}
				return false;
			case "isRefitPackageOptionSelected":
				String selectedPackageOption = memoryMap.get(MemKeys.LOCAL).getString("$option");
				HashMap<String, String> dialogData = parseDialogOptionId(selectedPackageOption);
				String finalMenuStateString = dialogData.get(DialogIdKeys.finalMenuState.toString());
				if (finalMenuStateString != null) {
					FinalMenuStates finalMenuState = FinalMenuStates.valueOf(finalMenuStateString);
					switch (finalMenuState.toString()) {
						case "preview":
							optionPanel.clearOptions();
							String chosenHullId = dialogData.get(DialogIdKeys.chosenHullId.toString());
							String chosenPackage = dialogData.get(DialogIdKeys.newPackage.toString());
							String chosenShipName = dialogData.get(DialogIdKeys.chosenShipName.toString());
							String previewHullId = getMake() + "_" + chosenPackage + "_" + chosenHullId + "_Hull";

							if (Boolean.parseBoolean(dialogData.get(DialogIdKeys.isPhase.toString()))
									&& !selectedPackageOption.contains(DialogIdKeys.replacePhaseCoils.toString())) {
								previewHullId = getMake() + "_" + chosenPackage + "_phase_" + chosenHullId + "_Hull";
							}

							FleetMemberAPI shipPreview = fleetData.addFleetMember(previewHullId);
							shipPreview.setShipName(chosenShipName);
							shipPreview.getVariant().addPermaMod("normal_torgue_" + chosenPackage + "_refit");
							shipPreview.getVariant().addPermaMod("armoredweapons");
							if (selectedPackageOption.contains(DialogIdKeys.replacePhaseCoils.toString())) {
								String phaseOption = dialogData.get(DialogIdKeys.replacePhaseCoils.toString());
								shipPreview.getVariant().addPermaMod("phase_" + phaseOption);
							}

							if (selectedPackageOption.contains(DialogIdKeys.newShield.toString())) {
								String newShield = dialogData.get(DialogIdKeys.newShield.toString());
								shipPreview.getVariant().addPermaMod("pow_" + newShield);
							}

							visual.showFleetMemberInfo(shipPreview);
							fleetData.removeFleetMember(shipPreview);

							optionPanel.addOption("Yes",
									selectedPackageOption + DialogIdKeys.newHullConfirmed + ":true;");

							String phaseCoilMenuId = selectedPackageOption.replace(
									DialogIdKeys.finalMenuState + ":" + FinalMenuStates.preview + ";",
									DialogIdKeys.finalMenuState + ":" + FinalMenuStates.phase_coils + ";");
							if (Boolean.parseBoolean(dialogData.get(DialogIdKeys.isPhase.toString()))) {
								optionPanel.addOption("Replace phase coils", phaseCoilMenuId);
							}

							if (selectedPackageOption.contains(DialogIdKeys.replacePhaseCoils.toString())) {
								optionPanel.setEnabled(phaseCoilMenuId, false);
								optionPanel.setTooltip(phaseCoilMenuId, "Replace phase coil option already selected");
							}

							String shieldSwapMenuId = selectedPackageOption.replace(
									DialogIdKeys.finalMenuState + ":" + FinalMenuStates.preview + ";",
									DialogIdKeys.finalMenuState + ":" + FinalMenuStates.shield_swap + ";");
							if (!Boolean.parseBoolean(dialogData.get(DialogIdKeys.isPhase.toString()))) {
								optionPanel.addOption("Install new shield", shieldSwapMenuId);
							}

							if (selectedPackageOption.contains(DialogIdKeys.newShield.toString())) {
								optionPanel.setEnabled(shieldSwapMenuId, false);
								optionPanel.setTooltip(shieldSwapMenuId, "A new shield has already been selected");
							}

							optionPanel.addOption("Exit", "CEO_Menu_Exit");
							optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false,
									false, false);
							return true;
						case "phase_coils":
							Global.getSector().getCampaignUI().addMessage("in phase menu");
							optionPanel.clearOptions();
							for (PhaseCoilReplacemnts phaseCoilReplacemnt : PhaseCoilReplacemnts.values()) {
								int originalCreditsCost = Integer
										.parseInt(dialogData.get(DialogIdKeys.creditsCost.toString()));
								int creditsCost = ((originalCreditsCost / 100) * phaseCoilReplacemnt.ordinal())
										+ phaseCoilReplacemnt.ordinal() * 150;
								int totalCost = originalCreditsCost + creditsCost;
								optionPanel.addOption(
										capitalize(phaseCoilReplacemnt.toString().replaceAll("_", " ") + " - "
												+ creditsCost + " credits"),
										selectedPackageOption.replace(
												DialogIdKeys.finalMenuState + ":" + FinalMenuStates.phase_coils + ";",
												DialogIdKeys.finalMenuState + ":" + FinalMenuStates.preview + ";")
												.replace(DialogIdKeys.creditsCost + ":" + originalCreditsCost + ";",
														DialogIdKeys.creditsCost + ":" + totalCost + ";")
												+ DialogIdKeys.replacePhaseCoils + ":" + phaseCoilReplacemnt + ";");
							}
							return true;
						case "shield_swap":
							optionPanel.clearOptions();
							Global.getSector().getCampaignUI().addMessage("in shield_swap menu");
							for (Shields newShield : Shields.values()) {
								int originalCreditsCost = Integer
										.parseInt(dialogData.get(DialogIdKeys.creditsCost.toString()));
								int creditsCost = ((originalCreditsCost / 100) * newShield.ordinal())
										+ (newShield.ordinal() + 1) * 1000;
								int totalCost = originalCreditsCost + creditsCost;
								optionPanel.addOption(
										capitalize(newShield.toString().replaceAll("_", " ") + " - " + creditsCost
												+ " credits"),
										selectedPackageOption.replace(
												DialogIdKeys.finalMenuState + ":" + FinalMenuStates.shield_swap + ";",
												DialogIdKeys.finalMenuState + ":" + FinalMenuStates.preview + ";")
												.replace(DialogIdKeys.creditsCost + ":" + originalCreditsCost + ";",
														DialogIdKeys.creditsCost + ":" + totalCost + ";")
												+ DialogIdKeys.newShield + ":" + newShield + ";");
							}

							optionPanel.addOption("Exit", "CEO_Menu_Exit");
							optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false,
									false, false);
							return true;
						default:
							break;
					}
				}

				return false;
			case "isRefitPackageOptionConfirmed":
				String confirmation = memoryMap.get(MemKeys.LOCAL).getString("$option");
				HashMap<String, String> dialogDataf = parseDialogOptionId(confirmation);
				if (Boolean.parseBoolean(dialogDataf.get(DialogIdKeys.newHullConfirmed.toString()))) {
					String shipName = dialogDataf.get(DialogIdKeys.chosenShipName.toString());
					String chosenHullId = dialogDataf.get(DialogIdKeys.chosenHullId.toString());
					String chosenPackage = dialogDataf.get(DialogIdKeys.newPackage.toString());
					String creditsCost = dialogDataf.get(DialogIdKeys.creditsCost.toString());
					playerFleet.getCargo().getCredits().subtract(Float.parseFloat(creditsCost));
					String originalHullId = dialogDataf.get(DialogIdKeys.originalHullId.toString());
					for (FleetMemberAPI fleetShip : fleetList) {
						if (fleetShip.getShipName().equals(shipName)
								&& fleetShip.getHullId().equals(originalHullId.toLowerCase())) {
							fleetData.scuttle(fleetShip);
							break;
						}
					}
					// int refitDuration = 2 + Math.round(Float.parseFloat(creditsCost) / 5000);
					int refitDuration = 1;
					Global.getSector().getCampaignUI()
							.addMessage(shipName + " " + capitalize(originalHullId.replaceAll("_", " "))
									+ " refiting will be complete in " + refitDuration + " days.");

					List<String> newHullMods = new ArrayList<String>();
					newHullMods.add("armoredweapons");
					if (confirmation.contains(DialogIdKeys.replacePhaseCoils.toString())) {
						newHullMods.add("phase_" + dialogDataf.get(DialogIdKeys.replacePhaseCoils.toString()));
					}
					if (confirmation.contains(DialogIdKeys.newShield.toString())) {
						newHullMods.add("pow_" + dialogDataf.get(DialogIdKeys.newShield.toString()));
					}

					List<SubmarketAPI> submarkets = market.getSubmarketsCopy();
					for (SubmarketAPI submarketAPI : submarkets) {
						if (submarketAPI.getName().equals("Storage")) {
							Random randomNumberGenerator = new Random();
							float random = randomNumberGenerator.nextInt(101);
							if (random <= 30) {
								newHullMods.add("normal_torgue_" + chosenPackage + "_refit");
							} else if (random <= 50) {
								newHullMods.add("superb_torgue_" + chosenPackage + "_refit");
							} else if (random <= 70) {
								newHullMods.add("legendary_torgue_" + chosenPackage + "_refit");
							} else if (random <= 90) {
								newHullMods.add("masterwork_torgue_" + chosenPackage + "_refit");
							}
							CargoAPI storageCargo = submarketAPI.getCargo();
							String newHull = getMake() + "_" + chosenPackage + "_" + chosenHullId + "_Hull";
							if (Boolean.parseBoolean(dialogDataf.get(DialogIdKeys.isPhase.toString()))
									&& !confirmation.contains(DialogIdKeys.replacePhaseCoils.toString())) {
								newHull = getMake() + "_" + chosenPackage + "_phase_" + chosenHullId + "_Hull";
							}
							Global.getSector().addScript(new RefitShip(newHullMods, storageCargo, newHull, shipName,
									capitalize(originalHullId.replaceAll("_", " ")), refitDuration));
						}
					}

					visual.fadeVisualOut();
					optionPanel.clearOptions();
					optionPanel.addOption("Exit", "CEO_Menu_Exit");
					optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false,
							false);
					return true;
				}
				return false;
		}

		return false;
	}

	private String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}

		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	private int getCreditsCost(String originalHullId, String chosenHull, RefitPackage refitPackage) {
		float credits = 0;
		FleetMemberAPI addShipToGetCost = fleetData.addFleetMember(originalHullId + "_Hull");
		FleetMemberAPI addResultShipToGetCost = fleetData
				.addFleetMember(getMake() + "_" + refitPackage.toString() + "_" + chosenHull.toLowerCase() + "_Hull");
		credits = addResultShipToGetCost.getBaseValue() - addShipToGetCost.getBaseValue();
		fleetData.removeFleetMember(addShipToGetCost);
		fleetData.removeFleetMember(addResultShipToGetCost);
		return credits > 0 ? Math.round(credits) : 0;
	}

	private String getMake() {
		if (person != null) {
			for (int i = 0; i < refitRepresentative.length; i++) {
				if (person.getName().getFirst().equals(refitRepresentative[i].Name)) {
					return refitRepresentative[i].Make.toLowerCase();
				}
			}
		}
		return "";
	}

	private HashMap<String, String> parseDialogOptionId(String optionId) {
		HashMap<String, String> result = new HashMap<String, String>();
		if (!optionId.contains(";")) {
			return result;
		}
		String[] pairs = optionId.split(";");
		for (String pair : pairs) {
			result.put(pair.substring(0, pair.indexOf(":")), pair.substring(pair.indexOf(":") + 1));
		}
		return result;
	}

	private RefitPackage getNextPackage(RefitPackage input) {
		RefitPackage[] values = RefitPackage.values();
		if (input.ordinal() + 1 == values.length) {
			return input;
		}
		int index = (input.ordinal() + 1) % values.length;
		return values[index];
	}

	private RefitPackage getPackageFromHullId(String hullId) {
		if (hullId.contains(RefitPackage.basic.toString())) {
			return RefitPackage.basic;
		}
		if (hullId.contains(RefitPackage.advanced.toString())) {
			return RefitPackage.advanced;
		}
		if (hullId.contains(RefitPackage.expert.toString())) {
			return RefitPackage.expert;
		}
		return null;
	}

	private FleetMemberAPI getFleetShipByName(String name) {
		for (FleetMemberAPI ship : fleetList) {
			if (ship.getShipName().equals(name)) {
				return ship;
			}
		}
		return null;
	}

	private String displayAvailableHulls(String hull) {
		String optionName = capitalize(hull) + " refiting";
		String optionId = DialogIdKeys.chosenHullId + ":" + hull + ";";
		for (RefitablePhase phaseShip : RefitablePhase.values()) {
			if (phaseShip.toString().equals(hull)) {
				optionId = optionId + DialogIdKeys.isPhase + ":true;";
			}
		}
		optionPanel.addOption(optionName, optionId, "No refitable ship of this hulltype found in your fleet");
		optionPanel.setEnabled(optionId, false);
		for (String fleetHullId : fleetHullIds) {
			if (fleetHullId.equalsIgnoreCase(hull)) {
				optionPanel.setEnabled(optionId, true);
				optionPanel.setTooltip(optionId, "");
				break;
			}
			if ((fleetHullId.contains(RefitPackage.basic.toString())
					|| fleetHullId.contains(RefitPackage.advanced.toString()))
					&& fleetHullId.substring(fleetHullId.toString().lastIndexOf("_") + 1).equalsIgnoreCase(hull)) {
				optionPanel.setEnabled(optionId, true);
				optionPanel.setTooltip(optionId, "");
				break;
			}
		}
		return "";
	}
}
