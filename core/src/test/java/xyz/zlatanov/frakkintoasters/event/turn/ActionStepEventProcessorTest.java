package xyz.zlatanov.frakkintoasters.event.turn;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTION;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON;

class ActionStepEventProcessorTest extends EventTestHarness<ActionStepEvent> {

    @Test
    void theCurrentPlayerMayActivateTheirLocation() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);

        execute(new ActionStepEvent());

        assertEquals(ACTION, game.turn().step());
        assertFollowup(one(new PlayerDecisionEvent<>(1, ActionEvent.class), new NoOpEvent(1)));
    }

    // "Activate his Viper: If the player is piloting a viper, he may activate it to move or to attack a Cylon ship."
    @Test
    void aPilotMayActivateTheirViper() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.player(1).pilot(viperAt(GALACTICA_SPACE_4_OCLOCK).id());

        execute(new ActionStepEvent());

        assertFollowup(one(new ActivateViperEvent(1), new NoOpEvent(1)));
    }

    @Test
    void strandedHeloTakesNoActions() {
        player(1).character(KARL_HELO_AGATHON);

        execute(new ActionStepEvent());

        assertEquals(ACTION, game.turn().step());
    }
}
