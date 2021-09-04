package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
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
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
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

public class CEO extends PaginatedOptions {
	public static enum Options {
		BRAWLER
	}	

	public static final List<String> refitRepresentative = Arrays.asList(new String[]{
		"Torgue", "Andreyevna", "Katagawa", "Jacobs", "Rhys", "Jack"
	});
	public static final List<String> refitableFrigateList = Arrays.asList(new String[]{
		"brawler", "scarab", "lasher"
	});
	public static final List<String> refitableDestroyerList = Arrays.asList(new String[]{
		"sunder"
	});
	public static final List<String> refitableCruiserList = Arrays.asList(new String[]{
		"eagle"
	});
	protected Logger logger = Global.getLogger(this.getClass());
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
	protected OptionPanelAPI options;

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

				OptionPanelAPI optionPanel = dialog.getOptionPanel();
				optionPanel.clearOptions();
				
				//List<String> refitableFrigades = Arrays.asList();
				int index = 0;
				for (String refitableShip : refitableFrigateList)
				{
					if (fleetHullIds.contains(refitableShip))  {
						optionPanel.addOption(capitalize(refitableShip) + " refit", index);
					} else {
						String optionId = capitalize(refitableShip) + " refit";
						optionPanel.addOption(optionId, index);
						optionPanel.setEnabled(index, false); 
					}
					index++;
				}
				optionPanel.addOption("Back", "CEO_Menu_Exit");
				break;
			case "confirmPurchase":

				break;
		}
		
		return false;
	}

	private String capitalize(String str) {
    if(str == null || str.isEmpty()) {
        return str;
    }

    return str.substring(0, 1).toUpperCase() + str.substring(1);
}
}
