package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.event.character.MoralCompassEvent;
import xyz.zlatanov.frakkintoasters.event.character.MoralCompassEventProcessor;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;

public class ChooseCrisisSectionEventProcessor extends EventProcessor<ChooseCrisisSectionEvent> {

    public static Followup resolve(GameView game, GameOps ops, CrisisCard card, Section section) {
        if (section == TOP && card.skillCheck() != null) {
            // The skill check is the top of the card, the alternate instruction below it is the other section.
            ops.skillCheck().start(card.skillCheck());
            return FollowupUtils.beforeSkillCheckFollowups(game);
        }
        return new CrisisCardFollowups(game).followup(card, section);
    }

    @Override
    public Followup process() {
        val card = event.card();
        val other = event.section() == TOP ? BOTTOM : TOP;
        // "Moral Compass" may change the choice before it resolves.
        return MoralCompassEventProcessor.changer(game, event.playerNumber(), card, other)
                .map(helo -> one(new MoralCompassEvent(helo, card, other), new ResolveCrisisSectionEvent(card, event.section())))
                .orElseGet(() -> resolve(game, ops, card, event.section()));
    }
}
