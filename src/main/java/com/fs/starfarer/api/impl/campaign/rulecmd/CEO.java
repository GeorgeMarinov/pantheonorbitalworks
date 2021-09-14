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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pantheonorbitalworks.RefitPackage;
import pantheonorbitalworks.RefitRepresentative;
import pantheonorbitalworks.RefitShip;
import pantheonorbitalworks.RefitableShip;

public class CEO extends PaginatedOptions {

	public final RefitRepresentative[] refitRepresentative = new RefitRepresentative[]{
		new RefitRepresentative("Mr. Torgue", "Torgue"), new RefitRepresentative("Andreyevna", "Vladof"), new RefitRepresentative("Katagawa", "Maliwan"), new RefitRepresentative("Jacobs", "Jacobs"), new RefitRepresentative("Rhys", "Atlas"), new RefitRepresentative("Handsome Jack", "Hyperion")
	};
	public final List<RefitableShip> refitableFrigateList = 
	Arrays.asList(new RefitableShip[]{
		new RefitableShip("brawler", 12000), new RefitableShip("scarab", 30000), new RefitableShip("lasher", 9000), 
	});
	public final List<String> refitableDestroyerList = Arrays.asList(new String[]{
		"sunder"
	});
	public final List<String> refitableCruiserList = Arrays.asList(new String[]{
		"eagle"
	});
	public final List<RefitPackage> packageList = Arrays.asList(new RefitPackage[]{
		new RefitPackage("Basic package", " basic package", 20), new RefitPackage("Advanced package", " advanced package", 50), new RefitPackage("Expert package", " expert package", 100)
	});
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
		
		//updatePointsInMemory(getPoints()); //store blueprint points

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
				
				int index = 0;
				for (RefitableShip refitableShip : refitableFrigateList)
				{
					String optionName = capitalize(refitableShip.HullId) + " refiting";
					String optionId = optionName + "_" + index;
					optionPanel.addOption(optionName, optionId);
					if (!fleetHullIds.contains(refitableShip.HullId)) {
						optionPanel.setEnabled(optionId, false); 
					}
					index++;
				}
				optionPanel.addOption("Back", "CEO_Menu_Exit");
				break;
			case "isRefitOptionSelected":
				String selectedOption = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (selectedOption.contains(" refiting")) {
					String shipName = selectedOption.substring(0, selectedOption.indexOf(" refiting"));
					visual.fadeVisualOut();
					optionPanel.clearOptions();
					int ind = 0;
					for (RefitPackage refitPackage : packageList) {
						int creditsCost = getCreditsCost(shipName, refitPackage.PercentCost);
						String optionName = shipName + refitPackage.DialogAppend + " - " + creditsCost + " credits";
						String optionId = optionName + "_" + ind;
						optionPanel.addOption(optionName, optionId);
						if (creditsCost == 0 || playerFleet.getCargo().getCredits().get() < creditsCost) {
							optionPanel.setEnabled(optionId, false); 
						}
						ind++;
					}
					optionPanel.addOption("Back", "CEO_Menu_Exit");	
					return true;
				}
				return false;
			case "isRefitPackageOptionSelected":
				String selectedPackageOption = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (selectedPackageOption.contains(" basic package") || selectedPackageOption.contains(" advanced package") || selectedPackageOption.contains(" expert package")) {
					String hullName = selectedPackageOption.substring(0, selectedPackageOption.indexOf(" "));
					String chosenPackage = selectedPackageOption.substring(selectedPackageOption.indexOf(" ") + 1, selectedPackageOption.indexOf("package") - 1);
					Global.getSector().getCampaignUI().addMessage("normal_" + getMake() + "_" + chosenPackage + "_" + hullName.toLowerCase() + "_Hull");
					FleetMemberAPI shipPreview = fleetData.addFleetMember("normal_" + getMake() + "_" + chosenPackage + "_" + hullName.toLowerCase() + "_Hull");
					visual.showFleetMemberInfo(shipPreview);
					fleetData.removeFleetMember(shipPreview);
					 
					optionPanel.clearOptions();
					optionPanel.addOption("Yes", selectedPackageOption + "!yes");	
					optionPanel.addOption("Back", "CEO_Menu_Exit");	
					return true;
				}
				return false;
			case "isRefitPackageOptionConfirmed":
				String confirmation = memoryMap.get(MemKeys.LOCAL).getString("$option");
				if (confirmation.contains("!yes")) {
					String shipName = "";
					String chosenPackage = confirmation.substring(confirmation.indexOf(" ") + 1, confirmation.indexOf("package") - 1);
					String creditsCost = confirmation.substring(confirmation.indexOf("-") + 1, confirmation.indexOf(" credits"));
					playerFleet.getCargo().getCredits().subtract(Float.parseFloat(creditsCost));
					String hullName = confirmation.substring(0, confirmation.indexOf(" "));
					for (FleetMemberAPI fleetShip : fleetList) {
						if (fleetShip.getHullId().equals(hullName.toLowerCase())) {
							shipName = fleetShip.getShipName();
							fleetData.scuttle(fleetShip);
							break;
						}
					}
					int refitDuration = Math.round(Float.parseFloat(creditsCost) / 1000);
					Global.getSector().getCampaignUI().addMessage(shipName + " " + hullName + " refiting will be complete in " + refitDuration + " days.");

					List<SubmarketAPI> submarkets = market.getSubmarketsCopy();
					for (SubmarketAPI submarketAPI : submarkets) {
						if (submarketAPI.getName().equals("Storage")) {
							Random randomNumberGenerator = new Random();
							float random = randomNumberGenerator.nextInt(101);
							String shipQuality = "";
							if (random <= 30) {
								shipQuality = "normal_";
							} else if (random <= 50) {
								shipQuality = "superb_";
							} else if (random <= 70) {
								shipQuality = "legendary_";
							} else if (random <= 90) {
								shipQuality = "masterwork_";
							}
							CargoAPI storageCargo = submarketAPI.getCargo();
							String newHull = shipQuality + getMake() + "_" + chosenPackage + "_" + hullName.toLowerCase() + "_Hull";
							Global.getSector().addScript(new RefitShip(storageCargo, newHull, shipName, hullName, refitDuration));
							
						}
					}

					visual.fadeVisualOut();
					optionPanel.clearOptions();
					optionPanel.addOption("Back", "CEO_Menu_Exit");	
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

	private int getCreditsCost(String shipName, int percentCost){
		// for (RefitableShip refitableShip : refitableFrigateList) {
		// 	if (capitalize(refitableShip.HullId).equals(shipName)) {
		// 		return refitableShip.BaseValue / 100 * percentCost;
		// 	}
		// }
		
		FleetMemberAPI addShipToGetCost = fleetData.addFleetMember(shipName.toLowerCase() + "_Hull");
		// ShipVariantAPI vt = addShipToGetCost.getVariant();
		// vt.clear();
		int credits = (int)addShipToGetCost.getBaseValue() / 100 * percentCost;
		fleetData.removeFleetMember(addShipToGetCost);
		return credits;
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
}
