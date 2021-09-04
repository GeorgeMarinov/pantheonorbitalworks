package pantheonorbitalworks;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import java.awt.Color;
import java.util.Map;
import org.lwjgl.input.Keyboard;

public class RefitInteractionDialogPlugin implements InteractionDialogPlugin, CoreInteractionListener {
	
	private InteractionDialogAPI dialog;
	private TextPanelAPI textPanel;
	private OptionPanelAPI options;
	private VisualPanelAPI visual;
	
	private CampaignFleetAPI playerFleet;
	private SectorEntityToken station;
	private PersonAPI person;
	
	//private static final Color HIGHLIGHT_COLOR = Global.getSettings().getColor("buttonShortcut");
	
	public void init(InteractionDialogAPI dialog) {
        Global.getLogger(this.getClass()).info("RefitInteractionDialogPlugin init");
		this.dialog = dialog;
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		station = dialog.getInteractionTarget();
        if (station != null) {
            person = station.getActivePerson();
        }
		
		visual.setVisualFade(0.25f, 0.25f);
	
		dialog.setOptionOnEscape("Leave", "Leave");
		
		optionSelected(null, "Init");
	}
	
	private EngagementResultAPI lastResult = null;
	public void backFromEngagement(EngagementResultAPI result) {
		// no combat here, so this won't get called
	}
	
	public void optionSelected(String text, Object optionData) {
		if (optionData == null) return;
		
		if (text != null) {
			textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
		}
		
		if (optionData == "Init") {
			// if (station.getFaction().getRelationship(playerFleet.getFaction().getId()) >= 0) {
            //                 addText(getString("approach"));
            //             } else {
            //                 addText("The Station weapons activate, you are forced to flee.");
            //             }
			createInitialOptions();
            if  (station.getCustomInteractionDialogImageVisual() != null) {
				visual.showImageVisual(station.getCustomInteractionDialogImageVisual());
			} else {
				visual.showImagePortion("illustrations", "hound_hangar", 800, 800, 0, 0, 400, 400);
			}
		} else if (optionData == "Refit") {
			textPanel.addParagraph("Refit");
			options.clearOptions();
			//visual.showCore(CoreUITabId.REFIT, station, station.getFaction().isNeutralFaction(), this);
		} else if (optionData == "Leave") {
			Global.getSector().setPaused(false);
			dialog.dismiss();
		}
	}
	
	// private void performRepairs() {
	// 	addText(getString("repair"));
	// 	float supplies = playerFleet.getCargo().getSupplies();
	// 	float needed = playerFleet.getLogistics().getTotalRepairSupplyCost();
		
	// 	textPanel.highlightLastInLastPara("" + (int) needed, HIGHLIGHT_COLOR);
		
	// 	for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
	// 		member.getStatus().repairFully();
	// 		float max = member.getRepairTracker().getMaxCR();
	// 		float curr = member.getRepairTracker().getBaseCR();
	// 		if (max > curr) {
	// 			member.getRepairTracker().applyCREvent(max - curr, "Repaired at station");
	// 		}
	// 	}
	// 	if (needed > 0) {
	// 		playerFleet.getCargo().removeSupplies(needed);
	// 	}
	// }
	
	private void createInitialOptions() {
		options.clearOptions();
		
		
		//if (station.getFaction().isNeutralFaction()) {
			options.addOption("Make use of the dockyard's refitting facilities", "Refit");
			options.setShortcut("Refit", Keyboard.KEY_R, false, false, false, true);
		//}
		
		// if (station.getFaction().getRelationship(playerFleet.getFaction().getId()) >= 0) {
                    
        //     options.addOption("Trade, or hire personnel", OptionId.TRADE_CARGO);
		// 	options.setShortcut(OptionId.TRADE_CARGO, Keyboard.KEY_I, false, false, false, true);
		// 	options.addOption("Buy or sell ships", OptionId.TRADE_SHIPS, null);
		// 	options.setShortcut(OptionId.TRADE_SHIPS, Keyboard.KEY_F, false, false, false, true);
		// 	options.addOption("Make use of the dockyard's refitting facilities", OptionId.REFIT);
		// 	options.setShortcut(OptionId.REFIT, Keyboard.KEY_R, false, false, false, true);
                    
		// 	float needed = playerFleet.getLogistics().getTotalRepairSupplyCost();
		// 	float supplies = playerFleet.getCargo().getSupplies();
		// 	options.addOption("Repair your ships at the station's dockyard", OptionId.REPAIR_ALL);

		// 	if (needed <= 0) {
		// 		options.setEnabled(OptionId.REPAIR_ALL, false);
		// 		options.setTooltip(OptionId.REPAIR_ALL, getString("repairTooltipAlreadyRepaired"));
		// 	} else if (supplies < needed) {
		// 		options.setEnabled(OptionId.REPAIR_ALL, false);
		// 		options.setTooltip(OptionId.REPAIR_ALL, getString("repairTooltipNotEnough"));
		// 		options.setTooltipHighlightColors(OptionId.REPAIR_ALL, HIGHLIGHT_COLOR, HIGHLIGHT_COLOR);
		// 		options.setTooltipHighlights(OptionId.REPAIR_ALL, "" + (int) needed, "" + (int) supplies);
		// 	} else {
		// 		options.setTooltip(OptionId.REPAIR_ALL, getString("repairTooltip"));
		// 		options.setTooltipHighlightColors(OptionId.REPAIR_ALL, HIGHLIGHT_COLOR, HIGHLIGHT_COLOR);
		// 		options.setTooltipHighlights(OptionId.REPAIR_ALL, "" + (int) needed, "" + (int) supplies);
		// 	}
		// }
		
		options.addOption("Leave", "Leave");
	}
	
	
	//private OptionId lastOptionMousedOver = null;
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public void advance(float amount) {
		
	}
	
	// private void addText(String text) {
	// 	textPanel.addParagraph(text);
	// }
	
	// private void appendText(String text) {
	// 	textPanel.appendToLastParagraph(" " + text);
	// }
	
	// private String getString(String id) {
	// 	String str = Global.getSettings().getString("stationInteractionDialog", id);

	// 	String fleetOrShip = "fleet";
	// 	if (playerFleet.getFleetData().getMembersListCopy().size() == 1) {
	// 		fleetOrShip = "ship";
	// 		if (playerFleet.getFleetData().getMembersListCopy().get(0).isFighterWing()) {
	// 			fleetOrShip = "fighter wing";
	// 		}
	// 	}
	// 	str = str.replaceAll("\\$fleetOrShip", fleetOrShip);
	// 	str = str.replaceAll("\\$stationName", station.getFullName());
		
	// 	float needed = playerFleet.getLogistics().getTotalRepairSupplyCost();
	// 	float supplies = playerFleet.getCargo().getSupplies();
	// 	str = str.replaceAll("\\$supplies", "" + (int) supplies);
	// 	str = str.replaceAll("\\$repairSupplyCost", "" + (int) needed);

	// 	return str;
	// }
	

	public Object getContext() {
		return null;
	}

	public void coreUIDismissed() {
		optionSelected(null, "Init");
	}

	public Map<String, MemoryAPI> getMemoryMap() {
		return null;
	}
}