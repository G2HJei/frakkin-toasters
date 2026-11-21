package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CHIEF_GALEN_TYROL;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.BLIND_DEVOTION;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.LEADERSHIP;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TACTICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.INBOUND_NUKES;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.WATER_SABOTAGED;

class BlindDevotionEventProcessorTest extends EventTestHarness<BlindDevotionEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(CHIEF_GALEN_TYROL);
        moveTo(HANGAR_DECK, CHIEF_GALEN_TYROL);
    }

    // The cards of the named type count 0, whatever their printed strength.
    @Test
    void theNamedTypeCountsZero() {
        ops.skillCheck().createDestinyDeck().start(WATER_SABOTAGED).playFromDestinyDeck(2);
        val leadership = new SkillCard(4, EXECUTIVE_ORDER);
        val tactics = new SkillCard(3, LAUNCH_SCOUT);
        player(1).gainSkillCards(leadership, tactics);
        ops.skillCheck().playCards(1, leadership, tactics);

        execute(new BlindDevotionEvent(1, LEADERSHIP));
        ops.skillCheck().shuffleAndDivideCards();

        assertEquals(LEADERSHIP, game.activeSkillCheck().zeroStrengthColor());
        assertEquals(0, game.activeSkillCheck().cardValue(leadership));
        assertEquals(3, game.activeSkillCheck().cardValue(tactics));
        assertEquals(0, player(1).miracleTokens());
        assertTrue(game.turn().hasUsed(1, BLIND_DEVOTION));
    }

    @Test
    void notOnceTotaled() {
        ops.skillCheck().start(WATER_SABOTAGED);
        assertNotEquals(NONE, BlindDevotionEventProcessor.offer(game));

        val card = new SkillCard(1, EXECUTIVE_ORDER);
        player(1).gainSkillCards(card);
        ops.skillCheck().playCards(1, card);
        ops.skillCheck().shuffleAndDivideCards();
        ops.skillCheck().totalStrength(1);

        assertEquals(NONE, BlindDevotionEventProcessor.offer(game));
        assertInvalid(new BlindDevotionEvent(1, TACTICS));
    }

    @Test
    void notOnASuperCrisisCheck() {
        ops.skillCheck().start(INBOUND_NUKES);

        assertEquals(NONE, BlindDevotionEventProcessor.offer(game));
        assertInvalid(new BlindDevotionEvent(1, TACTICS));
    }

    @Test
    void onlyWithAMiracleToken() {
        ops.skillCheck().start(WATER_SABOTAGED);
        ops.player(1).loseMiracleTokens();

        assertInvalid(new BlindDevotionEvent(1, TACTICS));
    }
}
