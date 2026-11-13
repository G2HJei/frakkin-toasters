package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.AIRLOCK;

class AirlockEventProcessorTest extends EventTestHarness<AirlockEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(HELENA_CAIN);
        moveTo(Location.AIRLOCK, HELENA_CAIN);
        player(2).character(GAIUS_BALTAR);
        moveTo(RESEARCH_LAB, GAIUS_BALTAR);
        player(3).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
    }

    // "Action: Choose a character and pass this skill check to execute him."
    @Test
    void shouldStartTheAirlockSkillCheckOnTheChosenCharacter() {
        assertTrue(LocationEvent.canAct(game, player(1)));

        execute(new AirlockEvent(1, 2));

        assertSkillCheckTriggered(AIRLOCK);
        assertEquals(2, game.activeSkillCheck().targetPlayerNumber());
    }

    // A revealed Cylon ignores an execution (rule 6.1.2.1).
    @Test
    void shouldNotChooseARevealedCylon() {
        player(2).loyaltyCards()
                .addOnTop(CYLON_SEND_TO_BRIG)
                .reveal(CYLON_SEND_TO_BRIG);
        assertInvalid(new AirlockEvent(1, 2));
    }

    @Test
    void shouldNotChooseSomeoneWhoIsNotPlaying() {
        assertInvalid(new AirlockEvent(1, 4));
    }

    // Characters may move into a damaged location, but may not use its action (rule 11.7.2.2).
    @Test
    void shouldHaveNoActionWhenDamaged() {
        pegasusBoard.damage(Location.AIRLOCK);

        assertFalse(LocationEvent.canAct(game, player(1)));
        assertInvalid(new AirlockEvent(1, 2));
    }
}
