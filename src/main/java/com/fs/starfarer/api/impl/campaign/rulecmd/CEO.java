package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemQuantity;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.Misc.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pantheonorbitalworks.ConvertWeapon;
import pantheonorbitalworks.Helpers;
import pantheonorbitalworks.PhaseCoilReplacemnts;
import pantheonorbitalworks.RefitPackage;
import pantheonorbitalworks.RefitRepresentative;
import pantheonorbitalworks.RefitShip;
import pantheonorbitalworks.RefitableCapital;
import pantheonorbitalworks.RefitableCruiser;
import pantheonorbitalworks.RefitableDestroyer;
import pantheonorbitalworks.RefitableFrigade;
import pantheonorbitalworks.RefitableLargeBallistic;
import pantheonorbitalworks.RefitableLargeEnergy;
import pantheonorbitalworks.RefitableLargeMissile;
import pantheonorbitalworks.RefitableMediumBallistic;
import pantheonorbitalworks.RefitableMediumEnergy;
import pantheonorbitalworks.RefitableMediumMissile;
import pantheonorbitalworks.RefitablePhase;
import pantheonorbitalworks.RefitableSmallBallistic;
import pantheonorbitalworks.RefitableSmallEnergy;
import pantheonorbitalworks.RefitableSmallMissile;
import pantheonorbitalworks.Shields;

public class CEO extends PaginatedOptions {

	public final RefitRepresentative[] refitRepresentative = new RefitRepresentative[] {
			new RefitRepresentative("Mr. Torgue", "Torgue"), new RefitRepresentative("Andreyevna", "Vladof"),
			new RefitRepresentative("Katagawa Jr.", "Maliwan"), new RefitRepresentative("Jacobs", "Jacobs"),
			new RefitRepresentative("Rhys Strongfork", "Atlas"), new RefitRepresentative("Handsome Jack", "Hyperion") };

	public enum DialogIdKeys {
		chosenHullId, originalHullPackage, isUpgrade, creditsCost, newPackage, newHullConfirmed, chosenShipName,
		originalHullId, isPhase, replacePhaseCoils, finalMenuState, newShield, chosenShipSize, chosenWeaponSize,
		chosenWeaponType, weaponToConvert, quantityWeaponsToConvert, weaponConfirmed
	}

	public enum FinalMenuStates {
		preview, phase_coils, shield_swap
	}

	public enum ShipSize {
		frigate, destroyer, cruiser, capital
	}

	public enum WeaponType {
		ballistic, energy, missile
	}

	protected CampaignFleetAPI playerFleet;
	protected FleetDataAPI fleetData;
	protected List<FleetMemberAPI> fleetList;
	protected List<String> fleetHullIds = new ArrayList<>();
	protected SectorEntityToken entity;
	protected MarketAPI market;
	protected PersonAPI person;
	protected FactionAPI faction;
	protected OptionPanelAPI optionPanel;
	protected VisualPanelAPI visual;

	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
			Map<String, MemoryAPI> memoryMap) {
		String arg = params.get(0).getString(memoryMap);

		this.dialog = dialog;
		this.memoryMap = memoryMap;

		entity = dialog.getInteractionTarget();
		market = entity.getMarket();
		playerFleet = Global.getSector().getPlayerFleet();
		fleetData = playerFleet.getFleetData();
		fleetList = fleetData.getMembersListCopy();
		person = dialog.getInteractionTarget().getActivePerson();
		optionPanel = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		for (FleetMemberAPI ship : fleetList) {
			fleetHullIds.add(ship.getHullId());
		}

		switch (arg) {
			case "init":
				// visual.fadeVisualOut();
				break;
			case "isRefitRepresentative":
				SectorEntityToken sectorEntity = dialog.getInteractionTarget();
				if (sectorEntity != null) {
					PersonAPI person = sectorEntity.getActivePerson();
					if (person != null) {
						for (int i = 0; i < refitRepresentative.length; i++) {
							if (person.getName().getFirst().equals(refitRepresentative[i].Name)) {
								dialog.getTextPanel().clear();
								dialog.getTextPanel().addPara(
										"HI NEW BEST FRIEND, I'M TORGUE FROM THE TORGUE CORPORATION AND I AM SUPER DUPER PISSED THAT LASER GUNS EXISTS. THEY ARE MADE OF LIGHT WHICH DOESN'T EVEN EXPLODE! LIKE WHAT ?!");
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
			case "chooseWeaponSize":
				originalPlugin = dialog.getPlugin();

				dialog.setPlugin(this);
				init(dialog);

				visual.fadeVisualOut();
				optionPanel.clearOptions();

				for (WeaponSize weaponSize : WeaponSize.values()) {
					optionPanel.addOption("Convert " + weaponSize,
							DialogIdKeys.chosenWeaponSize + ":" + weaponSize + ";");
				}

				optionPanel.addOption("Exit", "CEO_Menu_Exit");
				optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false,
						false);
				break;
			case "chooseHullType":
				String option = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (option.contains(DialogIdKeys.chosenShipSize.toString())) {
					dialog.getTextPanel().addPara("WHAT?!");
					visual.fadeVisualOut();
					optionPanel.clearOptions();
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

					optionPanel.addOption("Exit", "CEO_Menu_Exit");
					optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false,
							false,
							false);
				}
				break;
			case "chooseWeaponType":
				String weaponSize = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (weaponSize.contains(DialogIdKeys.chosenWeaponSize.toString())) {
					visual.fadeVisualOut();
					optionPanel.clearOptions();
					HashMap<String, String> dialogData = parseDialogOptionId(weaponSize);
					String chosenWeaponSizeString = dialogData.get(DialogIdKeys.chosenWeaponSize.toString());
					if (chosenWeaponSizeString != null) {
						for (WeaponType weaponType : WeaponType.values()) {
							optionPanel.addOption("Convert " + weaponType, DialogIdKeys.chosenWeaponSize + ":"
									+ chosenWeaponSizeString + ";" + DialogIdKeys.chosenWeaponType + ":" + weaponType
									+ ";");
						}
					}

					optionPanel.addOption("Exit", "CEO_Menu_Exit");
					optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false,
							false);
				}
				break;
			case "chooseShipToRefit":
				String chosenHull = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (chosenHull.contains(DialogIdKeys.chosenHullId.toString())
						&& !chosenHull.contains(DialogIdKeys.chosenShipName.toString())) {
					dialog.getTextPanel().addParagraph(
							"WE AT THE TORGUE CORPORATION SINCERELY BELIVE THAT FITTING SHIPS WITH EXPLOSIVES IS F*CKING AWESOME!");
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
			case "chooseWeaponToRefit":
				String chosenWeapon = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (chosenWeapon.contains(DialogIdKeys.chosenWeaponSize.toString())
						&& chosenWeapon.contains(DialogIdKeys.chosenWeaponType.toString())) {
					visual.fadeVisualOut();
					optionPanel.clearOptions();
					HashMap<String, String> dialogData = parseDialogOptionId(chosenWeapon);
					String chosenWeaponSize = dialogData.get(DialogIdKeys.chosenWeaponSize.toString());
					String chosenWeaponType = dialogData.get(DialogIdKeys.chosenWeaponType.toString());
					if (chosenWeaponSize.equals(WeaponSize.SMALL.toString())
							&& chosenWeaponType.equals(WeaponType.ballistic.toString())) {
						for (RefitableSmallBallistic weapon : RefitableSmallBallistic.values()) {
							optionPanel.addOption("Convert " + Helpers.weaponIdToLabel(weapon.toString()),
									DialogIdKeys.weaponToConvert + ":" + weapon.toString() + ";");
						}
					}
					if (chosenWeaponSize.equals(WeaponSize.MEDIUM.toString())
							&& chosenWeaponType.equals(WeaponType.ballistic.toString())) {
						for (RefitableMediumBallistic weapon : RefitableMediumBallistic.values()) {
							optionPanel.addOption("Convert " + Helpers.weaponIdToLabel(weapon.toString()),
									DialogIdKeys.weaponToConvert + ":" + weapon.toString() + ";");
						}
					}
					if (chosenWeaponSize.endsWith(WeaponSize.LARGE.toString())
							&& chosenWeaponType.equals(WeaponType.ballistic.toString())) {
						for (RefitableLargeBallistic weapon : RefitableLargeBallistic.values()) {
							optionPanel.addOption("Convert " + Helpers.weaponIdToLabel(weapon.toString()),
									DialogIdKeys.weaponToConvert + ":" + weapon.toString() + ";");
						}
					}
					if (chosenWeaponSize.equals(WeaponSize.SMALL.toString())
							&& chosenWeaponType.equals(WeaponType.missile.toString())) {
						for (RefitableSmallMissile weapon : RefitableSmallMissile.values()) {
							optionPanel.addOption("Convert " + Helpers.weaponIdToLabel(weapon.toString()),
									DialogIdKeys.weaponToConvert + ":" + weapon.toString() + ";");
						}
					}
					if (chosenWeaponSize.equals(WeaponSize.MEDIUM.toString())
							&& chosenWeaponType.equals(WeaponType.missile.toString())) {
						for (RefitableMediumMissile weapon : RefitableMediumMissile.values()) {
							optionPanel.addOption("Convert " + Helpers.weaponIdToLabel(weapon.toString()),
									DialogIdKeys.weaponToConvert + ":" + weapon.toString() + ";");
						}
					}
					if (chosenWeaponSize.endsWith(WeaponSize.LARGE.toString())
							&& chosenWeaponType.equals(WeaponType.missile.toString())) {
						for (RefitableLargeMissile weapon : RefitableLargeMissile.values()) {
							optionPanel.addOption("Convert " + Helpers.weaponIdToLabel(weapon.toString()),
									DialogIdKeys.weaponToConvert + ":" + weapon.toString() + ";");
						}
					}
					if (chosenWeaponSize.equals(WeaponSize.SMALL.toString())
							&& chosenWeaponType.equals(WeaponType.energy.toString())) {
						for (RefitableSmallEnergy weapon : RefitableSmallEnergy.values()) {
							optionPanel.addOption("Convert " + Helpers.weaponIdToLabel(weapon.toString()),
									DialogIdKeys.weaponToConvert + ":" + weapon.toString() + ";");
						}
					}
					if (chosenWeaponSize.equals(WeaponSize.MEDIUM.toString())
							&& chosenWeaponType.equals(WeaponType.energy.toString())) {
						for (RefitableMediumEnergy weapon : RefitableMediumEnergy.values()) {
							optionPanel.addOption("Convert " + Helpers.weaponIdToLabel(weapon.toString()),
									DialogIdKeys.weaponToConvert + ":" + weapon.toString() + ";");
						}
					}
					if (chosenWeaponSize.endsWith(WeaponSize.LARGE.toString())
							&& chosenWeaponType.equals(WeaponType.energy.toString())) {
						for (RefitableLargeEnergy weapon : RefitableLargeEnergy.values()) {
							optionPanel.addOption("Convert " + Helpers.weaponIdToLabel(weapon.toString()),
									DialogIdKeys.weaponToConvert + ":" + weapon.toString() + ";");
						}
					}

					optionPanel.addOption("Exit", "CEO_Menu_Exit");
					optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false,
							false);
				}
				break;
			case "isRefitOptionSelected": {
				String selectedOption = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (selectedOption.contains(DialogIdKeys.chosenShipName.toString())
						&& !selectedOption.contains(DialogIdKeys.creditsCost.toString())) {
					dialog.getTextPanel().addParagraph(
							"HOW ARE WE GOING TO REFIT IT? OH PROBABLY GET A TEAM HERE TO CREFULLY DISASSEMBLE IT AND WHAT THE F*CK DO YOU THINK ?! WE'RE GONNA BLOW IT UP!");
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
			}
			case "isRefitWeaponSelected": {
				String selectedWeapon = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (selectedWeapon.contains(DialogIdKeys.weaponToConvert.toString())) {
					HashMap<String, String> dialogData = parseDialogOptionId(selectedWeapon);
					String chosenWeaponData = dialogData.get(DialogIdKeys.weaponToConvert.toString());
					String baseWeaponId = chosenWeaponData.split("_")[1];
					visual.fadeVisualOut();
					optionPanel.clearOptions();
					Boolean playerHasChosenWeapon = false;
					int creditsCostPerWeapon = 0;
					playerFleet.getCargo().addWeapons("pow_" + chosenWeaponData, 1);
					int baseWeaponCost = 0;
					int convertedWeaponCost = 0;
					int stackSize = 0;
					List<CargoStackAPI> stacks = playerFleet.getCargo().getStacksCopy();
					for (CargoStackAPI stack : stacks) {
						Object stackData = stack.getData();
						if (stackData != null && stackData.toString().equals(baseWeaponId)) {
							baseWeaponCost = stack.getBaseValuePerUnit();
							playerHasChosenWeapon = true;
							stackSize = (int) stack.getSize();
						}
						if (stackData != null && stackData.toString().equals("pow_" + chosenWeaponData)) {
							convertedWeaponCost = stack.getBaseValuePerUnit();
							playerFleet.getCargo().removeWeapons("pow_" + chosenWeaponData, 1);
						}
					}
					if (playerHasChosenWeapon) {
						creditsCostPerWeapon = convertedWeaponCost - baseWeaponCost;
					}

					String singleOptionName = "Convert 1 " + Helpers.weaponIdToLabel(chosenWeaponData) + " -"
							+ creditsCostPerWeapon
							+ " credits";
					String singleOptionId = selectedWeapon + DialogIdKeys.creditsCost + ":" + creditsCostPerWeapon + ";"
							+ DialogIdKeys.quantityWeaponsToConvert + ":" + 1 + ";" + DialogIdKeys.finalMenuState + ":"
							+ FinalMenuStates.preview + ";";
					optionPanel.addOption(singleOptionName, singleOptionId);
					if (creditsCostPerWeapon == 0) {
						optionPanel.setEnabled(singleOptionId, false);
						optionPanel.setTooltip(singleOptionId, "No weapon in inventory to convert");
					}
					if (playerFleet.getCargo().getCredits().get() < creditsCostPerWeapon) {
						optionPanel.setEnabled(singleOptionId, false);
						optionPanel.setTooltip(singleOptionId, "Not enough credits to convert");
					}

					String fiveOptionName = "Convert 5 " + Helpers.weaponIdToLabel(chosenWeaponData) + " -"
							+ creditsCostPerWeapon * 5
							+ " credits";
					String fiveOptionId = selectedWeapon + DialogIdKeys.creditsCost + ":" + creditsCostPerWeapon + ";"
							+ DialogIdKeys.quantityWeaponsToConvert + ":" + 5 + ";" + DialogIdKeys.finalMenuState + ":"
							+ FinalMenuStates.preview + ";";
					optionPanel.addOption(fiveOptionName, fiveOptionId);
					if (stackSize < 5) {
						optionPanel.setEnabled(fiveOptionId, false);
						optionPanel.setTooltip(fiveOptionId, "Not enough weapons in inventory to convert");
					}
					if (playerFleet.getCargo().getCredits().get() < creditsCostPerWeapon * 5) {
						optionPanel.setEnabled(fiveOptionId, false);
						optionPanel.setTooltip(fiveOptionId, "Not enough credits to convert");
					}

					String tenOptionName = "Convert 10 " + Helpers.weaponIdToLabel(chosenWeaponData) + " -"
							+ creditsCostPerWeapon * 10
							+ " credits";
					String tenOptionId = selectedWeapon + DialogIdKeys.creditsCost + ":" + creditsCostPerWeapon + ";"
							+ DialogIdKeys.quantityWeaponsToConvert + ":" + 10 + ";" + DialogIdKeys.finalMenuState + ":"
							+ FinalMenuStates.preview + ";";
					optionPanel.addOption(tenOptionName, tenOptionId);
					if (stackSize < 10) {
						optionPanel.setEnabled(tenOptionId, false);
						optionPanel.setTooltip(tenOptionId, "Not enough weapons in inventory to convert");
					}
					if (playerFleet.getCargo().getCredits().get() < creditsCostPerWeapon * 10) {
						optionPanel.setEnabled(tenOptionId, false);
						optionPanel.setTooltip(tenOptionId, "Not enough credits to convert");
					}

					String allOptionName = "Convert all " + Helpers.weaponIdToLabel(chosenWeaponData) + " -"
							+ creditsCostPerWeapon * stackSize
							+ " credits";
					String allOptionId = selectedWeapon + DialogIdKeys.creditsCost + ":" + creditsCostPerWeapon + ";"
							+ DialogIdKeys.quantityWeaponsToConvert + ":" + stackSize + ";"
							+ "sameIdFailsafe:sameIdFailsafe;" + DialogIdKeys.finalMenuState + ":"
							+ FinalMenuStates.preview + ";";
					optionPanel.addOption(allOptionName, allOptionId);
					if (stackSize < 1) {
						optionPanel.setEnabled(allOptionId, false);
						optionPanel.setTooltip(allOptionId, "Not enough weapons in inventory to convert");
					}
					if (playerFleet.getCargo().getCredits().get() < creditsCostPerWeapon * stackSize) {
						optionPanel.setEnabled(allOptionId, false);
						optionPanel.setTooltip(allOptionId, "Not enough credits to convert");
					}

					optionPanel.addOption("Exit", "CEO_Menu_Exit");
					optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false,
							true);
					return true;
				}
				return false;
			}
			case "isRefitPackageOptionSelected": {
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

							String hullMods = "";
							hullMods = hullMods + ("normal_torgue_" + chosenPackage + "_refit");
							if (selectedPackageOption.contains(DialogIdKeys.replacePhaseCoils.toString())) {
								String phaseOption = dialogData.get(DialogIdKeys.replacePhaseCoils.toString());
								hullMods = hullMods + (",phase_" + phaseOption);
							}

							if (selectedPackageOption.contains(DialogIdKeys.newShield.toString())) {
								String newShield = dialogData.get(DialogIdKeys.newShield.toString());
								hullMods = hullMods + (",pow_" + newShield);
							}

							FleetMemberAPI shipPreview = fleetData.addFleetMember(previewHullId);
							String shipPreviewId = shipPreview.getId();
							shipPreview.setId(shipPreviewId + "modIds=" + hullMods);
							shipPreview.setShipName(chosenShipName);
							visual.showFleetMemberInfo(shipPreview);
							fleetData.removeFleetMember(shipPreview);
							dialog.getTextPanel().addParagraph(
									"NOW BEFORE WE GET STARTED YOU GOTTA DIGITALLY SIGN OUR LEGAL WAIVER.");
							optionPanel.addOption("Confirm",
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
			}
			case "isWeaponOptionSelected": {
				String selectedPackageOption = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (selectedPackageOption.contains(DialogIdKeys.finalMenuState.toString()) &&
						selectedPackageOption.contains(DialogIdKeys.creditsCost.toString()) &&
						selectedPackageOption.contains(DialogIdKeys.quantityWeaponsToConvert.toString())) {
					visual.fadeVisualOut();
					optionPanel.clearOptions();
					optionPanel.addOption("Confirm",
							selectedPackageOption + DialogIdKeys.weaponConfirmed + ":true;");
					optionPanel.addOption("Exit", "CEO_Menu_Exit");
					optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false,
							false, false);
					return true;
				}

				return false;
			}
			case "isRefitPackageOptionConfirmed": {
				String confirmation = memoryMap.get(MemKeys.LOCAL).getString("$option");
				HashMap<String, String> dialogDataf = parseDialogOptionId(confirmation);
				if (Boolean.parseBoolean(dialogDataf.get(DialogIdKeys.newHullConfirmed.toString()))) {
					dialog.getTextPanel().addParagraph(
							"JUST KIDDING! F*CK THE LEGAL WAIVER! YOU'RE IN TORGUE LAND NOW SUCKER! JUST WAIT FOR MY CREW TO FINISH WHILE I PLAY YOU A SICK GUITAIR SOLO! SQUEEDLYBAMBLYFEEDLYMEEDLYMOWWWWWWWWWOWWWWWWWOWWWWWWWWOWWWWWWNGGGGGGGGG!");
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

					String newHullMods = "";

					List<SubmarketAPI> submarkets = market.getSubmarketsCopy();
					String manufacturer = getMake();
					for (SubmarketAPI submarketAPI : submarkets) {
						if (submarketAPI.getName().equals("Storage")) {
							Random randomNumberGenerator = new Random();
							float random = randomNumberGenerator.nextInt(101);
							if (random < 50) {
								newHullMods = newHullMods + ("normal_" + manufacturer + "_" + chosenPackage + "_refit");
							} else if (random < 70) {
								newHullMods = newHullMods + ("superb_" + manufacturer + "_" + chosenPackage + "_refit");
							} else if (random < 80) {
								newHullMods = newHullMods
										+ ("legendary_" + manufacturer + "_" + chosenPackage + "_refit");
							} else if (random < 90) {
								newHullMods = newHullMods
										+ ("masterwork_" + manufacturer + "_" + chosenPackage + "_refit");
							} else if (random < 98) {
								newHullMods = newHullMods
										+ ("miracle_" + manufacturer + "_" + chosenPackage + "_refit");
							} else {
								newHullMods = newHullMods + ("normal_" + manufacturer + "_" + chosenPackage + "_refit");
							}

							if (confirmation.contains(DialogIdKeys.replacePhaseCoils.toString())) {
								newHullMods = newHullMods
										+ (",phase_" + dialogDataf.get(DialogIdKeys.replacePhaseCoils.toString()));
							}
							if (confirmation.contains(DialogIdKeys.newShield.toString())) {
								newHullMods = newHullMods
										+ (",pow_" + dialogDataf.get(DialogIdKeys.newShield.toString()));
							}

							CargoAPI storageCargo = submarketAPI.getCargo();
							String newHull = manufacturer + "_" + chosenPackage + "_" + chosenHullId + "_Hull";
							if (Boolean.parseBoolean(dialogDataf.get(DialogIdKeys.isPhase.toString()))
									&& !confirmation.contains(DialogIdKeys.replacePhaseCoils.toString())) {
								newHull = manufacturer + "_" + chosenPackage + "_phase_" + chosenHullId + "_Hull";
							}
							Global.getSector().addScript(new RefitShip(newHullMods, storageCargo, newHull, shipName,
									capitalize(originalHullId.replaceAll("_", " ")), refitDuration));
							break;
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
			case "isWeaponOptionConfirmed": {
				String weaponConvert = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (weaponConvert.contains(DialogIdKeys.weaponConfirmed.toString())) {
					HashMap<String, String> dialogDataw = parseDialogOptionId(weaponConvert);
					String weaponToConvert = dialogDataw.get(DialogIdKeys.weaponToConvert.toString());
					String weaponQuantity = dialogDataw.get(DialogIdKeys.quantityWeaponsToConvert.toString());
					String creditsPerWeapon = dialogDataw.get(DialogIdKeys.creditsCost.toString());
					playerFleet.getCargo().getCredits()
							.subtract(Float.parseFloat(creditsPerWeapon) * Integer.parseInt(weaponQuantity));
					List<CargoStackAPI> stacks = playerFleet.getCargo().getStacksCopy();
					for (CargoStackAPI stack : stacks) {
						Object stackData = stack.getData();
						if (stackData != null && stackData.toString().equals(weaponToConvert.split("_")[1])) {
							stack.subtract(Float.parseFloat(weaponQuantity));
						}
					}
					// int refitDuration = 1 + Math.round(Float.parseFloat(creditsCost) / 1000);
					int refitDuration = 1;
					Global.getSector().getCampaignUI()
							.addMessage(weaponQuantity + " " + Helpers.weaponIdToLabel(weaponToConvert)
									+ " conversion will be complete in " + refitDuration + " days.");

					List<SubmarketAPI> submarkets = market.getSubmarketsCopy();
					for (SubmarketAPI submarketAPI : submarkets) {
						if (submarketAPI.getName().equals("Storage")) {
							CargoAPI storageCargo = submarketAPI.getCargo();
							Global.getSector().addScript(new ConvertWeapon("pow_" + weaponToConvert, storageCargo,
									Integer.parseInt(weaponQuantity), refitDuration));
							break;
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
