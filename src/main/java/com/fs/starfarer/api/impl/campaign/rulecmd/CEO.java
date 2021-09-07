package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoPickerListener;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.Status;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

import pantheonorbitalworks.RefitPackage;
import pantheonorbitalworks.RefitShip;
import pantheonorbitalworks.RefitableShip;

public class CEO extends PaginatedOptions {

	public static final List<String> refitRepresentative = Arrays.asList(new String[]{
		"Torgue", "Andreyevna", "Katagawa", "Jacobs", "Rhys", "Jack"
	});
	public static final List<RefitableShip> refitableFrigateList = 
	Arrays.asList(new RefitableShip[]{
		new RefitableShip("brawler", 12000), new RefitableShip("scarab", 30000), new RefitableShip("lasher", 9000), 
	});
	public static final List<String> refitableDestroyerList = Arrays.asList(new String[]{
		"sunder"
	});
	public static final List<String> refitableCruiserList = Arrays.asList(new String[]{
		"eagle"
	});
	public static final List<RefitPackage> packageList = Arrays.asList(new RefitPackage[]{
		new RefitPackage("Basic package", " basic package", 20), new RefitPackage("Advanced package", " advanced package", 50), new RefitPackage("Expert package", " expert package", 100)
	});
	protected CampaignFleetAPI playerFleet;
	protected FleetDataAPI fleetData;
	protected List<FleetMemberAPI> fleetList;
	protected List<String> fleetHullIds = new ArrayList<>();
	protected SectorEntityToken entity;
	protected MarketAPI market;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected CargoAPI playerCargo;
	protected PersonAPI person;
	protected FactionAPI faction;
	protected float points;
	protected List<String> disabledOpts = new ArrayList<>();
	protected OptionPanelAPI optionPanel;

    @Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) 
	{
		String arg = params.get(0).getString(memoryMap);
		this.dialog = dialog;  
		this.memoryMap = memoryMap;
		
		entity = dialog.getInteractionTarget();
		market = entity.getMarket();
		text = dialog.getTextPanel();
		playerFleet = Global.getSector().getPlayerFleet();
		fleetData = playerFleet.getFleetData();
		fleetList = fleetData.getMembersListCopy();
		playerCargo = playerFleet.getCargo();
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		person = dialog.getInteractionTarget().getActivePerson();
		optionPanel = dialog.getOptionPanel();

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
					if (person != null && refitRepresentative.contains(person.getName().getLast())) {
						return true;
					}
				} else {
					return false;
				}
				break;
			case "refitFrigades":
				originalPlugin = dialog.getPlugin();  

				dialog.setPlugin(this);  
				init(dialog);

				optionPanel.clearOptions();
				
				//List<String> refitableFrigades = Arrays.asList();
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
					String shipName = "";
					String creditsCost = selectedPackageOption.substring(selectedPackageOption.indexOf("-") + 1, selectedPackageOption.indexOf(" credits"));
					playerFleet.getCargo().getCredits().subtract(Float.parseFloat(creditsCost));
					String hullName = selectedPackageOption.substring(0, selectedPackageOption.indexOf(" "));
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
							CargoAPI storageCargo = submarketAPI.getCargo();
							String newHull = "brawler_Hull";
							Global.getSector().addScript(new RefitShip(storageCargo, newHull, shipName, hullName, refitDuration));
						}
					}
					
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
}
