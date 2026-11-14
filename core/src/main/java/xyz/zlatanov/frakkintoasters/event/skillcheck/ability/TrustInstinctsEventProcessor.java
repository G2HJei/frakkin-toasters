package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.skillcheck.DetermineSkillCheckAbilitiesOrderEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class TrustInstinctsEventProcessor extends EventProcessor<TrustInstinctsEvent> {

    @Override
    public Followup process() {
        val cardsWithAbilities = ops.skillCheck()
                .playFromDestinyDeck(2, true)
                .stream()
                .filter(SkillCard::hasSkillCheckAbility)
                .toList();
        if (cardsWithAbilities.isEmpty()) {
            return NONE;
        }
        // The cards are in the check now, so their abilities resolve like the others: once per check, in the current
        // player's order.
        return single(new DetermineSkillCheckAbilitiesOrderEvent(game.turn().currentPlayerNumber(), cardsWithAbilities));
    }
}
