package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsOfColorEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CAPRICA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAPRICA_SIX;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.INTIMATE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;

class IntimateEventProcessorTest extends EventTestHarness<IntimateEvent> {

    private static final SkillCard ORDER = new SkillCard(1, EXECUTIVE_ORDER);

    @BeforeEach
    void setUp() {
        player(1).character(CAPRICA_SIX);
        player(2).character(WILLIAM_ADAMA);
        moveTo(CAPRICA, CAPRICA_SIX);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
    }

    // The other player then draws from the deck she names, outside his skill set too.
    @Test
    void takesARandomCardAndTheOtherDrawsFromTheNamedDeck() {
        player(2).gainSkillCards(ORDER);

        execute(new IntimateEvent(1, 2, TREACHERY));

        assertSkillCards(1, ORDER);
        assertSkillCards(2);
        assertTrue(game.turn().hasUsed(1, INTIMATE));
        assertFollowup(new DrawSkillCardsOfColorEvent(2, TREACHERY, 1));
    }

    @Test
    void anEmptyHandStillDraws() {
        execute(new IntimateEvent(1, 2, POLITICS));

        assertSkillCards(1);
        assertFollowup(new DrawSkillCardsOfColorEvent(2, POLITICS, 1));
    }

    @Test
    void notHerself() {
        assertInvalid(new IntimateEvent(1, 1, POLITICS));
    }

    @Test
    void notAnEmptyDeck() {
        politicsDeck.clear();

        assertInvalid(new IntimateEvent(1, 2, POLITICS));
    }

    @Test
    void notWithoutADeck() {
        assertInvalid(new IntimateEvent(1, 2, null));
    }

    @Test
    void notAnotherCharacter() {
        player(3).character(LAURA_ROSLIN);

        assertInvalid(new IntimateEvent(3, 2, POLITICS));
    }
}
