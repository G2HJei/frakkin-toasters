package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.CHOSEN;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.RANDOM;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SHARON_ATHENA_AGATHON;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FOR_LOVE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.DOGFIGHT;

class ForLoveEventProcessorTest extends EventTestHarness<ForLoveEvent> {

    private static final SkillCard TREACHERY = new SkillCard(0, DOGFIGHT); // what the fake deck hands her

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(SHARON_ATHENA_AGATHON);
        player(1).gainSkillCards(new SkillCard(1, CONSOLIDATE_POWER), new SkillCard(2, CONSOLIDATE_POWER),
                new SkillCard(3, CONSOLIDATE_POWER));
    }

    // She draws a Treachery card, and what is left of the discard is still forced.
    @Test
    void athenaMakesTheDiscardSmaller() {
        treacheryDeck.nextCard(TREACHERY);

        execute(new ForLoveEvent(2, 1, CHOSEN, 2));

        assertSkillCards(2, TREACHERY);
        assertTrue(game.turn().hasUsed(2, FOR_LOVE));
        assertFollowup(new ForcedDiscardEvent(1, CHOSEN, 1, false));
    }

    @Test
    void aDiscardOfOneBecomesNone() {
        execute(new ForLoveEvent(2, 1, RANDOM, 1));
    }

    @Test
    void onceATurn() {
        ops.player(2).useAbility(FOR_LOVE);

        assertInvalid(new ForLoveEvent(2, 1, CHOSEN, 2));
    }

    @Test
    void notForHerOwnDiscard() {
        player(2).gainSkillCards(new SkillCard(4, CONSOLIDATE_POWER));

        assertInvalid(new ForLoveEvent(2, 2, CHOSEN, 1));
    }

    @Test
    void notWhenThePlayerHoldsNothing() {
        player(3).character(SAUL_TIGH);

        assertInvalid(new ForLoveEvent(2, 3, CHOSEN, 2));
    }

    @Test
    void onlyAthena() {
        assertInvalid(new ForLoveEvent(1, 2, CHOSEN, 2));
    }
}
