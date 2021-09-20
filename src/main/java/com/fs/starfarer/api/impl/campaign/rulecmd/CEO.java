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
import pantheonorbitalworks.RefitableFrigade;
import pantheonorbitalworks.RefitablePhase;

public class CEO extends PaginatedOptions {

	public final RefitRepresentative[] refitRepresentative = new RefitRepresentative[]{
		new RefitRepresentative("Mr. Torgue", "Torgue"), new RefitRepresentative("Andreyevna", "Vladof"), new RefitRepresentative("Katagawa", "Maliwan"), new RefitRepresentative("Jacobs", "Jacobs"), new RefitRepresentative("Rhys", "Atlas"), new RefitRepresentative("Handsome Jack", "Hyperion")
	};

	public enum DialogIdKeys {
		chosenHullId, originalHullPackage, isUpgrade, creditsCost, newPackage, newHullConfirmed, chosenShipName, originalHullId, isPhase
	}
    
	protected CampaignFleetAPI playerFleet;
	protected FleetDataAPI fleetData;
	protected List<FleetMemberAPI> fleetList;
	protected List<String> fleetHullIds = new ArrayList<>();
	protected SectorEntityToken entity;
	protected MarketAPI market;
	//protected TextPanelAPI text;
	//protected CargoAPI playerCargo;
	protected PersonAPI person;
	protected FactionAPI faction;
	protected float points;
	protected List<String> disabledOpts = new ArrayList<>();
	protected OptionPanelAPI optionPanel;
	protected VisualPanelAPI visual;
	private Logger log = Global.getLogger(this.getClass());

    @Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) 
	{
		String arg = params.get(0).getString(memoryMap);
		this.dialog = dialog;  
		this.memoryMap = memoryMap;
		
		entity = dialog.getInteractionTarget();
		market = entity.getMarket();
		//text = dialog.getTextPanel();
		playerFleet = Global.getSector().getPlayerFleet();
		fleetData = playerFleet.getFleetData();
		fleetList = fleetData.getMembersListCopy();
		//playerCargo = playerFleet.getCargo();
		person = dialog.getInteractionTarget().getActivePerson();
		optionPanel = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		for (FleetMemberAPI ship : fleetList) {
			fleetHullIds.add(ship.getHullId());
		}

		switch (arg)
		{
			case "init":
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
			case "refitFrigades":
				originalPlugin = dialog.getPlugin();  

				dialog.setPlugin(this);  
				init(dialog);

				visual.fadeVisualOut();
				optionPanel.clearOptions();

				for (RefitableFrigade refitableShip : RefitableFrigade.values())
				{
					String optionName = capitalize(refitableShip.toString()) + " refiting";
					String optionId = DialogIdKeys.chosenHullId.toString() + ":" + refitableShip.toString() + ";";
					for (RefitablePhase phaseShip : RefitablePhase.values()) {
						if (phaseShip.toString().equals(refitableShip.toString())) {
							optionId = optionId + DialogIdKeys.isPhase + ":true;";
						}
					}
					optionPanel.addOption(optionName, optionId, "No refitable ship of this hulltype found in your fleet");
					optionPanel.setEnabled(optionId, false);
					for (String fleetHullId : fleetHullIds) {
						if (fleetHullId.equalsIgnoreCase(refitableShip.toString())) {
							optionPanel.setEnabled(optionId, true);
							optionPanel.setTooltip(optionId, "");
							break;
						}
						if ((fleetHullId.contains(RefitPackage.basic.toString()) || fleetHullId.contains(RefitPackage.advanced.toString())) && fleetHullId.substring(fleetHullId.toString().lastIndexOf("_") + 1).equalsIgnoreCase(refitableShip.toString())) {
							optionPanel.setEnabled(optionId, true);
							optionPanel.setTooltip(optionId, "");
							break;
						}
					}
				}
				optionPanel.addOption("Back", "CEO_Menu_Exit");
				optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false, false);
				break;
			case "chooseFrigadeToRefit":
				String chosenHull = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (chosenHull.contains(DialogIdKeys.chosenHullId.toString())) {
					visual.fadeVisualOut();
					optionPanel.clearOptions();
					HashMap<String, String> dialogData = parseDialogOptionId(chosenHull);
					String chosenHullId = dialogData.get(DialogIdKeys.chosenHullId.toString());
					for (FleetMemberAPI fleetShip : fleetList) {
						String fleetHullId = fleetShip.getHullId();
						String optionName = "Refit " + fleetShip.getShipName() + ", " + capitalize(fleetHullId.replaceAll("_", " "));
						String optionId = chosenHull;
						if ((fleetHullId.contains(RefitPackage.basic.toString()) || fleetHullId.contains(RefitPackage.advanced.toString())) && fleetHullId.substring(fleetHullId.lastIndexOf("_") + 1).equalsIgnoreCase(chosenHullId)) {
							RefitPackage originalHullPackage = getPackageFromHullId(fleetHullId);
							optionId = optionId + DialogIdKeys.isUpgrade.toString() + ":true;" + DialogIdKeys.originalHullPackage.toString() + ":" + originalHullPackage.toString() + ";" + DialogIdKeys.chosenShipName.toString() + ":" + fleetShip.getShipName() + ";" + DialogIdKeys.originalHullId.toString() + ":" + fleetShip.getHullId() +  ";";
							optionPanel.addOption(optionName, optionId);
						}
						if (fleetHullId.equals(chosenHullId)) {
							optionId = optionId + DialogIdKeys.chosenShipName.toString() + ":" + fleetShip.getShipName() + ";" + DialogIdKeys.originalHullId.toString() + ":" + fleetShip.getHullId() +  ";";
							optionPanel.addOption(optionName, optionId);
						}
					}
					optionPanel.addOption("Back", "CEO_Menu_Exit");
					optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false, false);
				}
				break;
			case "isRefitOptionSelected":
				String selectedOption = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (selectedOption.contains(DialogIdKeys.chosenShipName.toString())) {
					HashMap<String, String> dialogData = parseDialogOptionId(selectedOption);
					String chosenHullId = dialogData.get(DialogIdKeys.chosenHullId.toString());
					String originalHullId = dialogData.get(DialogIdKeys.originalHullId.toString());
					visual.fadeVisualOut();
					optionPanel.clearOptions();
					for (RefitPackage refitPackage : RefitPackage.values()) {
						Global.getSector().getCampaignUI().addMessage(dialogData.get(DialogIdKeys.isPhase.toString()));
						int creditsCost = getCreditsCost(originalHullId, Boolean.parseBoolean(dialogData.get(DialogIdKeys.isPhase.toString())) ? "phase_" + chosenHullId : chosenHullId, refitPackage);
						String optionName = capitalize(chosenHullId) + " " + refitPackage.toString() + " package - " + creditsCost + " credits";
						String optionId = selectedOption + DialogIdKeys.creditsCost.toString() + ":" + creditsCost + ";" + DialogIdKeys.newPackage.toString() + ":" + refitPackage.toString() + ";";
						optionPanel.addOption(optionName, optionId);
						if (creditsCost == 0 || playerFleet.getCargo().getCredits().get() < creditsCost) {
							optionPanel.setEnabled(optionId, false);
						}
					}
					optionPanel.addOption("Back", "CEO_Menu_Exit");
					optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false, false);
					return true;
				}
				return false;
			case "isRefitPackageOptionSelected":
				String selectedPackageOption = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (selectedPackageOption.contains(DialogIdKeys.creditsCost.toString())) {
					HashMap<String, String> dialogData = parseDialogOptionId(selectedPackageOption);
					String chosenHullId = dialogData.get(DialogIdKeys.chosenHullId.toString());
					String chosenPackage = dialogData.get(DialogIdKeys.newPackage.toString());
					String chosenShipName = dialogData.get(DialogIdKeys.chosenShipName.toString());
					String previewHullId =  getMake() + "_" + chosenPackage + "_" + chosenHullId + "_Hull";
					if (Boolean.parseBoolean(dialogData.get(DialogIdKeys.isPhase.toString()))) {
						previewHullId =  getMake() + "_" + chosenPackage + "_phase_" + chosenHullId + "_Hull";
					}
					FleetMemberAPI shipPreview = fleetData.addFleetMember(previewHullId);
					shipPreview.setShipName(chosenShipName);
					shipPreview.getVariant().addPermaMod("normal_torgue_" + chosenPackage + "_refit");
					visual.showFleetMemberInfo(shipPreview);
					fleetData.removeFleetMember(shipPreview);
					 
					optionPanel.clearOptions();
					optionPanel.addOption("Yes", selectedPackageOption + DialogIdKeys.newHullConfirmed.toString() + ":true;");	
					optionPanel.addOption("Back", "CEO_Menu_Exit");
					optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false, false);
					return true;
				}
				return false;
			case "isRefitPackageOptionConfirmed":
				String confirmation = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (confirmation.contains(DialogIdKeys.newHullConfirmed.toString())) {
					HashMap<String, String> dialogData = parseDialogOptionId(confirmation);
					String shipName = dialogData.get(DialogIdKeys.chosenShipName.toString());
					String chosenHullId = dialogData.get(DialogIdKeys.chosenHullId.toString());
					String chosenPackage = dialogData.get(DialogIdKeys.newPackage.toString());
					String creditsCost = dialogData.get(DialogIdKeys.creditsCost.toString());
					playerFleet.getCargo().getCredits().subtract(Float.parseFloat(creditsCost));
					String originalHullId = dialogData.get(DialogIdKeys.originalHullId.toString());
					for (FleetMemberAPI fleetShip : fleetList) {
						if (fleetShip.getHullId().equals(originalHullId.toLowerCase())) {
							fleetData.scuttle(fleetShip);
							break;
						}
					}
					int refitDuration = 2 + Math.round(Float.parseFloat(creditsCost) / 5000);
					Global.getSector().getCampaignUI().addMessage(shipName + " " + capitalize(originalHullId.replaceAll("_", " ")) + " refiting will be complete in " + refitDuration + " days.");

					List<SubmarketAPI> submarkets = market.getSubmarketsCopy();
					for (SubmarketAPI submarketAPI : submarkets) {
						if (submarketAPI.getName().equals("Storage")) {
							Random randomNumberGenerator = new Random();
							float random = randomNumberGenerator.nextInt(101);
							String refitHullModId = "";
							if (random <= 30) {
								refitHullModId = "normal_torgue_" + chosenPackage + "_refit";
							} else if (random <= 50) {
								refitHullModId = "superb_torgue_" + chosenPackage + "_refit";
							} else if (random <= 70) {
								refitHullModId = "legendary_torgue_" + chosenPackage + "_refit";
							} else if (random <= 90) {
								refitHullModId = "masterwork_torgue_" + chosenPackage + "_refit";
							}
							CargoAPI storageCargo = submarketAPI.getCargo();
							String newHull =  getMake() + "_" + chosenPackage + "_" + chosenHullId + "_Hull";
							if (Boolean.parseBoolean(dialogData.get(DialogIdKeys.isPhase.toString()))) {
								newHull =  getMake() + "_" + chosenPackage + "_phase_" + chosenHullId + "_Hull";
							}
							Global.getSector().addScript(new RefitShip(refitHullModId, storageCargo, newHull, shipName, capitalize(originalHullId.replaceAll("_", " ")), refitDuration));
							
						}
					}

					visual.fadeVisualOut();
					optionPanel.clearOptions();
					optionPanel.addOption("Back", "CEO_Menu_Exit");
					optionPanel.setShortcut("CEO_Menu_Exit", org.lwjgl.input.Keyboard.KEY_ESCAPE, false, false, false, false);
					return true;
				}
				return false;
		}
		
		return false;
	}

	private String capitalize(String str) {
    	if(str == null || str.isEmpty()) {
    	    return str;
    	}

    	return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	private int getCreditsCost(String originalHullId, String chosenHull, RefitPackage refitPackage){
		float credits = 0;
		FleetMemberAPI addShipToGetCost = fleetData.addFleetMember(originalHullId  + "_Hull");
		FleetMemberAPI addResultShipToGetCost = fleetData.addFleetMember(getMake() + "_"  + refitPackage.toString() + "_"  + chosenHull.toLowerCase() + "_Hull");
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

	private RefitPackage getPackageFromHullId(String hullId){
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
}
