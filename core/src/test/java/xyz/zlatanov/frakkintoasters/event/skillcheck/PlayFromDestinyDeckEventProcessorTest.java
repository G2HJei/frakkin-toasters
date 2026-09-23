package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.deck.CreateDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.deck.CreateDestinyDeckEventProcessor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.AT_ANY_COST;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BAIT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheck.ADMIRALS_QUARTERS;

class PlayFromDestinyDeckEventProcessorTest extends EventTestHarness<PlayFromDestinyDeckEvent> {

    @BeforeEach
    void setUp() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldAdd2DestinyDeckCardsToSkillCheck() {
        val skillCard1 = new SkillCard(1, AT_ANY_COST);
        val skillCard2 = new SkillCard(2, BAIT);
        destinyDeck.nextCard(skillCard1, skillCard2);

        execute(new PlayFromDestinyDeckEvent());

        assertEquals(List.of(skillCard1, skillCard2), game.activeSkillCheck().cards().cards());
    }

    @Test
    void shouldCreateNewDestinyDeckWhenEmpty() {
        new CreateDestinyDeckEventProcessor().execute(game, new CreateDestinyDeckEvent());
        destinyDeck.draw(10); //2 remaining

        execute(new PlayFromDestinyDeckEvent());

        assertEquals(12, destinyDeck.cards().size());
        assertTrue(destinyDeck.wasShuffled());
    }
}