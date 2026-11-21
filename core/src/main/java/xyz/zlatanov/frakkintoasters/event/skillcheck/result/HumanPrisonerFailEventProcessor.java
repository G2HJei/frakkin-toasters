package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;

public class HumanPrisonerFailEventProcessor extends EventProcessor<HumanPrisonerFailEvent> {

    @Override
    protected boolean isValid() {
        return game.activeSkillCheck() != null;
    }

    @Override
    public Followup process() {
        val cylon = game.activeSkillCheck().playedBy();
        if (cylon == null) {
            return NONE; // the check didn't come from a played Super Crisis Card, e.g. in a test
        }
        return one(playersFrom(game, cylon).stream()
                .filter(PlayerView::isHuman)
                .map(human -> (Event) new HumanPrisonerTakeHandEvent(cylon, human.number()))
                .toArray(Event[]::new));
    }
}
