package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.DamageHumanFighterEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.DamageHumanFighterEventProcessor.canBeDamaged;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.DOGFIGHT;

public class DogfightEventProcessor extends EventProcessor<DogfightEvent> {

    // Both a viper and a card, or neither to decline.
    @Override
    protected boolean isValid() {
        if (event.viperId() == null && event.removedCard() == null) {
            return true;
        }
        return event.viperId() != null && event.removedCard() != null
                && canBeDamaged(game, event.viperId())
                && isOtherCardInTheCheck(event.removedCard());
    }

    @Override
    public Followup process() {
        val removedCard = event.removedCard();
        if (removedCard == null) {
            return Followup.NONE;
        }
        ops.skillCheck().discard(removedCard);
        return single(new DamageHumanFighterEvent(event.viperId()));
    }

    // "1 other card": the "Dogfight" card being resolved stays, so removing a "Dogfight" card takes a second copy.
    private boolean isOtherCardInTheCheck(SkillCard card) {
        val skillCheck = game.activeSkillCheck();
        val copies = Stream.concat(skillCheck.matchingPile().cards().stream(), skillCheck.nonMatchingPile().cards().stream())
                .filter(card::equals)
                .count();
        return copies > (card.type() == DOGFIGHT ? 1 : 0);
    }
}
