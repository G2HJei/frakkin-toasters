package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.Collection;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.ENGINEERING;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.ESTABLISH_NETWORK;

public class RedTapeEventProcessor extends EventProcessor<RedTapeEvent> {

    @Override
    public Followup process() {
        val cardsToDiscard = Stream.of(
                        game.activeSkillCheck().matchingPile().cards(),
                        game.activeSkillCheck().nonMatchingPile().cards())
                .flatMap(Collection::stream)
                .filter(c -> cardValue(c) > 4)
                .toList();
        ops.skillCheck().discard(cardsToDiscard);
        return Followup.NONE;
    }

    //todo card value here and in QuickThinkingEventProcessor could be moved to utility method - in SkillCheck class?
    private int cardValue(SkillCard card) {
        val faceValue = card.value();
        val valueDoubled = card.type().color() == ENGINEERING &&
                game.activeSkillCheck().resolvedAbilities().contains(ESTABLISH_NETWORK);
        val valueMultiplier = valueDoubled ? 2 : 1;
        return faceValue * valueMultiplier;
    }
}
