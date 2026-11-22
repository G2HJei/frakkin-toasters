package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class ChooseCharacterForBrigEventProcessorTest extends EventTestHarness<ChooseCharacterForBrigEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(LEE_APOLLO_ADAMA);
        player(3).character(WILLIAM_ADAMA);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        moveTo(COMMAND, LEE_APOLLO_ADAMA);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
    }

    @Test
    void sendsTheChosenCharacter() {
        execute(new ChooseCharacterForBrigEvent(1, 2));

        assertFollowup(new SendToBrigEvent(2));
    }

    // "he cannot choose a character that is already there" (rule 9.2.3)
    @Test
    void notACharacterAlreadyInTheBrig() {
        moveTo(BRIG, LEE_APOLLO_ADAMA);

        assertInvalid(new ChooseCharacterForBrigEvent(1, 2));
    }

    // "He also cannot choose a revealed Cylon player"
    @Test
    void notARevealedCylon() {
        revealCylon();

        assertInvalid(new ChooseCharacterForBrigEvent(2, 1));
    }

    @Test
    void notSomeoneWhoIsNoPlayer() {
        assertInvalid(new ChooseCharacterForBrigEvent(1, 4));
    }
}
