package pantheonorbitalworks;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import java.util.List;
import pantheonorbitalworks.world.POW_Gen;

public class POW_modPlugin extends BaseModPlugin {

    @Override
    public void onNewGame() {
        new POW_Gen().generate(Global.getSector());
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        MarketAPI market = Global.getSector().getEconomy().getMarket("pow_patheon_planet");
        if (market != null) {
            List<PersonAPI> people = market.getPeopleCopy();
            for (PersonAPI person : people) {
                if (!person.getName().getFirst().equals("Mr. Torgue")) {
                    PersonAPI mrTorgue = Global.getFactory().createPerson();
                    mrTorgue.setGender(FullName.Gender.MALE);
                    mrTorgue.setRankId(Ranks.SPECIAL_AGENT);
                    mrTorgue.setPostId(Ranks.POST_SPECIAL_AGENT);
                    mrTorgue.getName().setFirst("Mr. Torgue");
                    mrTorgue.setPortraitSprite(Global.getSettings().getSpriteName("characters", "mrTorgue"));

                    market.getCommDirectory().addPerson(mrTorgue, 0);
                    market.addPerson(mrTorgue);

                    // PersonAPI handsomeJack = Global.getFactory().createPerson();
                    // handsomeJack.setGender(FullName.Gender.MALE);
                    // handsomeJack.setRankId(Ranks.SPECIAL_AGENT);
                    // handsomeJack.setPostId(Ranks.POST_SPECIAL_AGENT);
                    // handsomeJack.getName().setFirst("Handsome Jack");
                    // handsomeJack.setPortraitSprite(Global.getSettings().getSpriteName("characters",
                    // "handsomeJack"));

                    // market.getCommDirectory().addPerson(handsomeJack, 0);
                    // market.addPerson(handsomeJack);
                    break;
                }
            }
        }
    }
}
