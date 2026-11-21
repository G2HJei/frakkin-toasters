package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.character.ForcedDiscards;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.treachery;

public class CylonPowerDrawTreacheryEventProcessor extends EventProcessor<CylonPowerDrawTreacheryEvent> {

    static final int TREACHERY_CARDS_DRAWN = 2;

    @Override
    public Followup process() {
        val followups = new ArrayList<Followup>();
        // The revealer is no longer human, so the humans are the others, from his left.
        playersFrom(game, player.number()).stream()
                .filter(PlayerView::isHuman)
                .forEach(human -> followups.add(
                        all(ForcedDiscards.discardRandom(game, human.number(), 1), treachery(human.number(), 1))));
        followups.add(single(treachery(player.number(), TREACHERY_CARDS_DRAWN)));
        return all(followups.toArray(Followup[]::new));
    }
}
