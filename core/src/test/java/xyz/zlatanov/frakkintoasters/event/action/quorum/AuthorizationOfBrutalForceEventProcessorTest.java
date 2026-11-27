package xyz.zlatanov.frakkintoasters.event.action.quorum;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.DestroyRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCenturionEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyHeavyRaiderEvent;

import java.util.List;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_3_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_9_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.AUTHORIZATION_OF_BRUTAL_FORCE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class AuthorizationOfBrutalForceEventProcessorTest extends EventTestHarness<AuthorizationOfBrutalForceEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(AUTHORIZATION_OF_BRUTAL_FORCE);
    }

    // The raiders may come from different areas.
    @Test
    void threeRaidersAreDestroyed() {
        val a = raiderAt(GALACTICA_SPACE_3_OCLOCK).id();
        val b = raiderAt(GALACTICA_SPACE_3_OCLOCK).id();
        val c = raiderAt(GALACTICA_SPACE_9_OCLOCK).id();
        raiderAt(GALACTICA_SPACE_9_OCLOCK);

        execute(new AuthorizationOfBrutalForceEvent(1, List.of(a, b, c)));

        assertFollowup(all(single(new DestroyRaidersEvent(Set.of(a, b, c))),
                rollDie(new AuthorizationOfBrutalForceResultEvent())));
    }

    @Test
    void allTheRaidersWhenFewerAreLeft() {
        val a = raiderAt(GALACTICA_SPACE_3_OCLOCK).id();

        execute(new AuthorizationOfBrutalForceEvent(1, List.of(a)));

        assertFollowup(all(single(new DestroyRaidersEvent(Set.of(a))),
                rollDie(new AuthorizationOfBrutalForceResultEvent())));
    }

    @Test
    void notFewerRaidersThanItCould() {
        val a = raiderAt(GALACTICA_SPACE_3_OCLOCK).id();
        val b = raiderAt(GALACTICA_SPACE_3_OCLOCK).id();
        raiderAt(GALACTICA_SPACE_3_OCLOCK);

        assertInvalid(new AuthorizationOfBrutalForceEvent(1, List.of(a, b)));
    }

    @Test
    void oneHeavyRaider() {
        val heavyRaider = heavyRaiderAt(GALACTICA_SPACE_3_OCLOCK).id();

        execute(new AuthorizationOfBrutalForceEvent(1, List.of(heavyRaider)));

        assertFollowup(all(single(new DestroyHeavyRaiderEvent(heavyRaider)),
                rollDie(new AuthorizationOfBrutalForceResultEvent())));
    }

    @Test
    void oneCenturion() {
        val centurion = centurion();
        galacticaBoard.boardGalactica(centurion);

        execute(new AuthorizationOfBrutalForceEvent(1, List.of(centurion.id())));

        assertFollowup(all(single(new DestroyCenturionEvent(centurion.id())),
                rollDie(new AuthorizationOfBrutalForceResultEvent())));
    }

    @Test
    void notAHeavyRaiderWithARaider() {
        val raider = raiderAt(GALACTICA_SPACE_3_OCLOCK).id();
        val heavyRaider = heavyRaiderAt(GALACTICA_SPACE_3_OCLOCK).id();

        assertInvalid(new AuthorizationOfBrutalForceEvent(1, List.of(raider, heavyRaider)));
    }
}
