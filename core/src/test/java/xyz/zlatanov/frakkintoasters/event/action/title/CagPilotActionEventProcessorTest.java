package xyz.zlatanov.frakkintoasters.event.action.title;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ViperActivation;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.CAG_TITLE;
import static xyz.zlatanov.frakkintoasters.state.TurnLimit.CAG_PILOT_ACTION;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_5_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_7_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;

class CagPilotActionEventProcessorTest extends EventTestHarness<CagPilotActionEvent> {

    private static final ViperActivation LAUNCH = ViperActivation.launch(VIPER, GALACTICA_SPACE_7_OCLOCK);

    private int ownViperId;

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        cag(KARA_STARBUCK_THRACE);
        ownViperId = viperAt(GALACTICA_SPACE_5_OCLOCK).id();
        ops.player(1).pilot(ownViperId);
    }

    @Test
    void thePilotingCagActivatesAViperThenTakesAnotherAction() {
        execute(new CagPilotActionEvent(1, LAUNCH));

        assertTrue(game.turn().hasUsed(1, CAG_PILOT_ACTION));
        assertFollowup(all(new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_7_OCLOCK), new GrantEvent(1, ACTION, CAG_TITLE)));
    }

    @Test
    void onceATurn() {
        ops.useTurnLimit(1, CAG_PILOT_ACTION);

        assertInvalid(new CagPilotActionEvent(1, LAUNCH));
    }

    @Test
    void onlyWhilePiloting() {
        ops.ship(ownViperId).land(HANGAR_DECK);

        assertInvalid(new CagPilotActionEvent(1, LAUNCH));
    }

    @Test
    void onlyTheCag() {
        cag(WILLIAM_ADAMA);

        assertInvalid(new CagPilotActionEvent(1, LAUNCH));
    }

    @Test
    void notHisOwnViper() {
        assertInvalid(new CagPilotActionEvent(1, ViperActivation.move(ownViperId, GALACTICA_SPACE_7_OCLOCK)));
    }

    @Test
    void anUnmannedViperInSpace() {
        val unmanned = viperAt(GALACTICA_SPACE_5_OCLOCK);

        execute(new CagPilotActionEvent(1, ViperActivation.move(unmanned.id(), GALACTICA_SPACE_7_OCLOCK)));

        assertEquals(GALACTICA_SPACE_7_OCLOCK, locate(unmanned));
        assertFollowup(new GrantEvent(1, ACTION, CAG_TITLE));
    }
}
