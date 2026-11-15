package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ResolveCrisisCardEventProcessor extends EventProcessor<ResolveCrisisCardEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null;
    }

    @Override
    public Followup process() {
        val card = event.card();
        if (card.chooser() != null) {
            return new CrisisCardFollowups(game).choice(card); // an event crisis, or a skill check with an alternate
        }
        ops.skillCheck().start(card.skillCheck());
        return FollowupUtils.beforeSkillCheckFollowups(game);
    }
}
