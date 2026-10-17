package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.Collection;
import java.util.stream.Stream;

public class RedTapeEventProcessor extends EventProcessor<RedTapeEvent> {

    @Override
    public Followup process() {
        val cardsToDiscard = Stream.of(
                        game.activeSkillCheck().matchingPile().cards(),
                        game.activeSkillCheck().nonMatchingPile().cards())
                .flatMap(Collection::stream)
                .filter(c -> c.value() > 4)
                .toList();
        ops.skillCheck().discard(cardsToDiscard);
        return Followup.NONE;
    }
}
