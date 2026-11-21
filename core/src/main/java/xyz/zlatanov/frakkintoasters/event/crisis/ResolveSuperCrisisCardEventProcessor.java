package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;

public class ResolveSuperCrisisCardEventProcessor extends EventProcessor<ResolveSuperCrisisCardEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null;
    }

    @Override
    public Followup process() {
        val card = event.card();
        if (card.chooser() != null) {
            return new SuperCrisisCardFollowups(game).choice(card);
        }
        // The check remembers who played the card, e.g. for "Human Prisoner".
        ops.skillCheck().startSuperCrisis(card.skillCheck(), player.number());
        return FollowupUtils.beforeSkillCheckFollowups(game);
    }
}
