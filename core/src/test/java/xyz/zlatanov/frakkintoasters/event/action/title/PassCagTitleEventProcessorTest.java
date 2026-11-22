package xyz.zlatanov.frakkintoasters.event.action.title;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ViperActivation;
import xyz.zlatanov.frakkintoasters.event.officials.MakeCagEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_6_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_REDUCE_MORALE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;

class PassCagTitleEventProcessorTest extends EventTestHarness<PassCagTitleEvent> {

    private static final ViperActivation LAUNCH = ViperActivation.launch(VIPER, GALACTICA_SPACE_4_OCLOCK);

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        cag(KARA_STARBUCK_THRACE);
    }

    // The title passes before what the activation brings, so "Alert Viper Pilot"'s action can't pass it again
    // (F-b9a6d6).
    @Test
    void theCagActivatesAViperThenPassesTheTitle() {
        execute(new PassCagTitleEvent(1, LAUNCH, 2));

        assertFollowup(all(new MakeCagEvent(WILLIAM_ADAMA), new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_4_OCLOCK)));
    }

    @Test
    void anUnmannedViperInSpaceCanBeTheOne() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new PassCagTitleEvent(1, ViperActivation.move(viper.id(), GALACTICA_SPACE_6_OCLOCK), 3));

        assertFollowup(new MakeCagEvent(LAURA_ROSLIN));
    }

    @Test
    void onlyTheCag() {
        assertInvalid(new PassCagTitleEvent(2, LAUNCH, 3));
    }

    @Test
    void notToHimself() {
        assertInvalid(new PassCagTitleEvent(1, LAUNCH, 1));
    }

    @Test
    void notToSomeoneWhoIsntPlaying() {
        assertInvalid(new PassCagTitleEvent(1, LAUNCH, 4));
    }

    @Test
    void notToACharacterInTheBrig() {
        moveTo(BRIG, WILLIAM_ADAMA);

        assertInvalid(new PassCagTitleEvent(1, LAUNCH, 2));
    }

    @Test
    void notToARevealedCylon() {
        player(2).loyaltyCards().addOnTop(CYLON_REDUCE_MORALE).reveal(CYLON_REDUCE_MORALE);

        assertInvalid(new PassCagTitleEvent(1, LAUNCH, 2));
    }

    @Test
    void theActivationHasToBeAnUnmannedViper() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        ops.player(2).pilot(viper.id());

        assertInvalid(new PassCagTitleEvent(1, ViperActivation.move(viper.id(), GALACTICA_SPACE_6_OCLOCK), 3));
    }

    @Test
    void notWithoutAnActivation() {
        assertInvalid(new PassCagTitleEvent(1, null, 3));
    }

    @Test
    void notWithoutAViperToActivate() {
        assertTrue(PassCagTitleEventProcessor.canAct(game, player(1)));

        for (val type : List.of(Viper.class, ViperMarkVII.class, AssaultRaptor.class)) {
            while (galacticaBoard.removeFromReserves(type).isPresent()) {
                // takes every fighter of the type out of the "Reserves"
            }
        }

        assertFalse(PassCagTitleEventProcessor.canAct(game, player(1)));
        assertInvalid(new PassCagTitleEvent(1, LAUNCH, 2));
    }
}
