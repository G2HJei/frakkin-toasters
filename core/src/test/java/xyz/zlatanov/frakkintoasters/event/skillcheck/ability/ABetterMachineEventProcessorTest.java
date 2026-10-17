package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.A_BETTER_MACHINE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BAIT;

class ABetterMachineEventProcessorTest extends EventTestHarness<ABetterMachineEvent> {

    @Test
    void shouldShuffle2TreacheryCardsIntoDestinyDeck() {
        val treachery1 = new SkillCard(0, A_BETTER_MACHINE);
        val treachery2 = new SkillCard(0, BAIT);
        treacheryDeck.nextCard(treachery1, treachery2);

        execute(new ABetterMachineEvent());

        assertTrue(destinyDeck.cards().containsAll(Set.of(treachery1, treachery2)));
    }
}