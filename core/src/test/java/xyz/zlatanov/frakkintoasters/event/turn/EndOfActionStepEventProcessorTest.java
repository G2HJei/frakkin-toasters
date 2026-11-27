package xyz.zlatanov.frakkintoasters.event.turn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LOUANNE_KAT_KATRAINE;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.STIM_JUNKIE;

class EndOfActionStepEventProcessorTest extends EventTestHarness<EndOfActionStepEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LOUANNE_KAT_KATRAINE);
    }

    // "Stim Junkie": where her turn started, she goes to "Sickbay".
    @Test
    void katWhereSheStartedGoesToSickbay() {
        moveTo(HANGAR_DECK, LOUANNE_KAT_KATRAINE);
        ops.noteStartLocation(HANGAR_DECK);

        execute(new EndOfActionStepEvent());

        assertTrue(game.turn().hasUsed(1, STIM_JUNKIE));
        assertFollowup(new SendToSickbayEvent(1));
    }

    @Test
    void katWhoMovedStays() {
        moveTo(ARMORY, LOUANNE_KAT_KATRAINE);
        ops.noteStartLocation(HANGAR_DECK);

        execute(new EndOfActionStepEvent());
    }

    // A space area counts like a location.
    @Test
    void katInTheSameSpaceAreaGoesToSickbay() {
        moveTo(HANGAR_DECK, LOUANNE_KAT_KATRAINE);
        ops.player(1).pilot(viperAt(GALACTICA_SPACE_5_OCLOCK).id());
        ops.noteStartLocation(GALACTICA_SPACE_5_OCLOCK);

        execute(new EndOfActionStepEvent());

        assertFollowup(new SendToSickbayEvent(1));
    }

    @Test
    void katInSickbayAlreadyStays() {
        moveTo(SICKBAY, LOUANNE_KAT_KATRAINE);
        ops.noteStartLocation(SICKBAY);

        execute(new EndOfActionStepEvent());
    }

    @Test
    void nothingOnceTheTurnEndedEarly() {
        moveTo(HANGAR_DECK, LOUANNE_KAT_KATRAINE);
        ops.noteStartLocation(HANGAR_DECK);
        ops.endTurnEarly();

        execute(new EndOfActionStepEvent());
    }
}
