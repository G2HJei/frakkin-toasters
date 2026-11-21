package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_REDUCE_MORALE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.POLITICALLY_ADROIT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;

class PoliticallyAdroitEventProcessorTest extends EventTestHarness<PoliticallyAdroitEvent> {

    private static final SkillCard ORDER = new SkillCard(1, EXECUTIVE_ORDER);

    @BeforeEach
    void setUp() {
        player(1).character(ELLEN_TIGH);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(SAUL_TIGH);
        moveTo(ADMIRALS_QUARTERS, ELLEN_TIGH);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(COMMAND, SAUL_TIGH);
        player(1).gainSkillCards(ORDER);
    }

    // She gives the card away, then draws 2 from her skill set.
    @Test
    void givesACardToDrawTwo() {
        execute(new PoliticallyAdroitEvent(1, 2, ORDER));

        assertSkillCards(1);
        assertSkillCards(2, ORDER);
        assertTrue(game.turn().hasUsed(1, POLITICALLY_ADROIT));
        assertFollowup(new PlayerDecisionEvent<>(1, DrawSkillCardsEvent.class, DRAW_EXACTLY_2));
    }

    @Test
    void notToAPlayerElsewhere() {
        assertInvalid(new PoliticallyAdroitEvent(1, 3, ORDER));
    }

    @Test
    void notToHerself() {
        assertInvalid(new PoliticallyAdroitEvent(1, 1, ORDER));
    }

    @Test
    void notACardSheDoesntHold() {
        assertInvalid(new PoliticallyAdroitEvent(1, 2, new SkillCard(2, EXECUTIVE_ORDER)));
    }

    @Test
    void notToARevealedCylon() {
        player(2).loyaltyCards().addOnTop(CYLON_REDUCE_MORALE).reveal(CYLON_REDUCE_MORALE);

        assertInvalid(new PoliticallyAdroitEvent(1, 2, ORDER));
    }
}
