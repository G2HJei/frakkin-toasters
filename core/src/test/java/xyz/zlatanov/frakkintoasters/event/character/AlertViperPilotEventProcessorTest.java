package xyz.zlatanov.frakkintoasters.event.character;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;
import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.event.action.ResetActingPlayerEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.ALERT_VIPER_PILOT;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_6_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PRESS_ROOM;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;

class AlertViperPilotEventProcessorTest extends EventTestHarness<AlertViperPilotEvent> {

    // A viper placed from the "Reserves" on another player's turn: he pilots it and acts, then that player goes on.
    @Test
    void apolloPilotsTheViper() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(LEE_APOLLO_ADAMA);
        moveTo(COMMAND, LEE_APOLLO_ADAMA);
        val viper = viperAt(GALACTICA_SPACE_6_OCLOCK);
        assertEquals(one(new AlertViperPilotEvent(2, viper.id()), new NoOpEvent(2)),
                AlertViperPilotEventProcessor.offer(game, List.of(viper.id())));

        execute(new AlertViperPilotEvent(2, viper.id()));

        assertTrue(galacticaBoard.pilotedFighter(LEE_APOLLO_ADAMA).isPresent());
        assertFollowup(all(new GrantEvent(2, ACTION, ALERT_VIPER_PILOT), new ResetActingPlayerEvent()));
    }

    @Test
    void onlyFromAGalacticaLocationOtherThanTheBrig() {
        player(1).character(LEE_APOLLO_ADAMA);
        val viper = viperAt(GALACTICA_SPACE_6_OCLOCK);

        moveTo(BRIG, LEE_APOLLO_ADAMA);
        assertEquals(NONE, AlertViperPilotEventProcessor.offer(game, List.of(viper.id())));
        moveTo(PRESS_ROOM, LEE_APOLLO_ADAMA);
        assertEquals(NONE, AlertViperPilotEventProcessor.offer(game, List.of(viper.id())));
        assertInvalid(new AlertViperPilotEvent(1, viper.id()));
    }

    @Test
    void notAPilotedViper() {
        player(1).character(LEE_APOLLO_ADAMA);
        player(2).character(LAURA_ROSLIN);
        moveTo(COMMAND, LEE_APOLLO_ADAMA);
        val viper = viperAt(GALACTICA_SPACE_6_OCLOCK);
        viper.pilot(LAURA_ROSLIN);

        assertEquals(NONE, AlertViperPilotEventProcessor.offer(game, List.of(viper.id())));
        assertInvalid(new AlertViperPilotEvent(1, viper.id()));
    }

    // In a skill check ("Protect the Fleet" launched it), he boards but takes no action: it could start another skill
    // check inside this one (F-f8b0c5).
    @Test
    void noActionInASkillCheck() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(LEE_APOLLO_ADAMA);
        moveTo(COMMAND, LEE_APOLLO_ADAMA);
        val viper = viperAt(GALACTICA_SPACE_6_OCLOCK);
        game.startSkillCheck(ADMIRALS_QUARTERS);

        execute(new AlertViperPilotEvent(2, viper.id()));

        assertTrue(galacticaBoard.pilotedFighter(LEE_APOLLO_ADAMA).isPresent());
    }
}
