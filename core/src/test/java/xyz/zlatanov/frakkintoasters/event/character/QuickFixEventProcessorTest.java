package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CALLANDRA_CALLY_TYROL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.WATER_SABOTAGED;

class QuickFixEventProcessorTest extends EventTestHarness<QuickFixEvent> {

    private final SkillCard leadership = new SkillCard(3, EXECUTIVE_ORDER);
    private final SkillCard tactics    = new SkillCard(2, LAUNCH_SCOUT);

    @BeforeEach
    void setUp() {
        player(1).character(CALLANDRA_CALLY_TYROL);
        moveTo(HANGAR_DECK, CALLANDRA_CALLY_TYROL);
        player(2).character(LAURA_ROSLIN);
        ops.changeCurrentPlayer(1);
        ops.skillCheck().start(WATER_SABOTAGED);
        player(2).gainSkillCards(leadership, tactics);
        ops.skillCheck().playCards(2, leadership, tactics);
        ops.skillCheck().shuffleAndDivideCards();
    }

    @Test
    void discardsARevealedCard() {
        execute(new QuickFixEvent(1, tactics));

        assertFalse(game.activeSkillCheck().dividedCards().contains(tactics));
        assertTrue(tacticsDeck.discardedCards().contains(tactics));
    }

    @Test
    void notOnAnotherPlayersTurn() {
        ops.changeCurrentPlayer(2);

        assertEquals(NONE, QuickFixEventProcessor.offer(game));
        assertInvalid(new QuickFixEvent(1, tactics));
    }

    @Test
    void notACardOutsideTheCheck() {
        assertInvalid(new QuickFixEvent(1, new SkillCard(5, EXECUTIVE_ORDER)));
    }
}
