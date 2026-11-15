package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;

import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;

public class ChooseCrisisSectionEventProcessor extends EventProcessor<ChooseCrisisSectionEvent> {

    @Override
    public Followup process() {
        val card = event.card();
        if (event.section() == TOP && card.skillCheck() != null) {
            // The skill check is the top of the card, the alternate instruction below it is the other section.
            ops.skillCheck().start(card.skillCheck());
            return FollowupUtils.beforeSkillCheckFollowups(game);
        }
        return new CrisisCardFollowups(game).followup(card, event.section());
    }
}
