package xyz.zlatanov.frakkintoasters.event.action.title;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.DestroyRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.OpenInterruptWindowEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBasestarEvent;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterBasestarOrCenturionDestroyed;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class NukeBasestarEventProcessorTest extends EventTestHarness<NukeBasestarEvent> {

    private static final OpenInterruptWindowEvent MAJOR_VICTORY_WINDOW =
            new OpenInterruptWindowEvent(new AfterBasestarOrCenturionDestroyed());

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void aLowRollDamagesTheBasestarTwice(int roll) {
        val basestar = basestarAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new NukeBasestarEvent(1, basestar.id(), roll));

        assertFollowup(all(new DamageBasestarEvent(basestar.id()), new DamageBasestarEvent(basestar.id())));
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 6})
    void aMiddleRollDestroysTheBasestar(int roll) {
        val basestar = basestarAt(GALACTICA_SPACE_4_OCLOCK);
        raiderAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new NukeBasestarEvent(1, basestar.id(), roll));

        assertShipCount(GALACTICA_SPACE_4_OCLOCK, Basestar.class, 0);
        assertShipCount(GALACTICA_SPACE_4_OCLOCK, 1);
        assertFollowup(MAJOR_VICTORY_WINDOW);
    }

    @Test
    void aSevenDestroysTheBasestarAndThreeRaiders() {
        val basestar = basestarAt(GALACTICA_SPACE_4_OCLOCK);
        // raiders are all alike: the ones with the lowest ids go
        val raiders = Stream.generate(() -> raiderAt(GALACTICA_SPACE_4_OCLOCK).id())
                .limit(4)
                .sorted()
                .limit(3)
                .collect(Collectors.toSet());

        execute(new NukeBasestarEvent(1, basestar.id(), 7));

        assertShipCount(GALACTICA_SPACE_4_OCLOCK, Basestar.class, 0);
        assertFollowup(all(new DestroyRaidersEvent(raiders), MAJOR_VICTORY_WINDOW));
    }

    @Test
    void onlyABasestar() {
        val raider = raiderAt(GALACTICA_SPACE_4_OCLOCK);

        assertInvalid(new NukeBasestarEvent(1, raider.id(), 4));
    }

    @Test
    void onlyABasestarInSpace() {
        assertInvalid(new NukeBasestarEvent(1, basestar().id(), 4));
    }

    // An 8 destroys the whole space area, without a choice of basestar.
    @Test
    void notAnEight() {
        val basestar = basestarAt(GALACTICA_SPACE_4_OCLOCK);

        assertInvalid(new NukeBasestarEvent(1, basestar.id(), 8));
    }

    @Test
    void notBelowOne() {
        val basestar = basestarAt(GALACTICA_SPACE_4_OCLOCK);

        assertInvalid(new NukeBasestarEvent(1, basestar.id(), 0));
    }
}
