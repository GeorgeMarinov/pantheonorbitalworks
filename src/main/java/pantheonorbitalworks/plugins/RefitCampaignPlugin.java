package pantheonorbitalworks.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.OrbitalStationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import pantheonorbitalworks.RefitInteractionDialogPlugin;

public class RefitCampaignPlugin extends BaseCampaignPlugin {

    public static final List<String> refitRepresentative = Arrays.asList(new String[]{
		"Torgue", "Andreyevna", "Katagawa", "Jacobs", "Rhys", "Jack"
	});

    @Override
	public String getId() {
		return null;
	}
	
    @Override
	public boolean isTransient() {
		return false;
	}

    @Override
	public PluginPick pickInteractionDialogPlugin(Object param, SectorEntityToken interactionTarget) {
		Global.getLogger(this.getClass()).info(param);
		Global.getLogger(this.getClass()).info(interactionTarget);
		if (interactionTarget != null) {
			PersonAPI person = interactionTarget.getActivePerson();
			MarketAPI market = interactionTarget.getMarket();
			Global.getLogger(this.getClass()).info(person);
			Global.getLogger(this.getClass()).info(market);
			if (market != null) {
				List<PersonAPI> people = market.getPeopleCopy();
				for (PersonAPI p : people) {
					Global.getLogger(this.getClass()).info(p);
					if (refitRepresentative.contains(p.getName().getLast())) {
						Global.getLogger(this.getClass()).info("init plugin");
						return new PluginPick(new RefitInteractionDialogPlugin(), PickPriority.MOD_GENERAL);
					}
				}
			}
			if (person != null && refitRepresentative.contains(person.getName().getLast())) {
				Global.getLogger(this.getClass()).info("init plugin");
				return new PluginPick(new RefitInteractionDialogPlugin(), PickPriority.MOD_GENERAL);
			}
		}
		
		return null;
	}	
}