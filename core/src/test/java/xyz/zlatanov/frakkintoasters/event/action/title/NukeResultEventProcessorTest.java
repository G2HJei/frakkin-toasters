package xyz.zlatanov.frakkintoasters.event.action.title;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.DestroyRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.OpenInterruptWindowEvent;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterBasestarOrCenturionDestroyed;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_6_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class NukeResultEventProcessorTest extends EventTestHarness<NukeResultEvent> {

    private static final NukeResultEvent AT_4_OCLOCK = new NukeResultEvent(1, GALACTICA_SPACE_4_OCLOCK);

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        player(2).character(KARA_STARBUCK_THRACE);
        admiral(WILLIAM_ADAMA);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 6})
    void withoutABasestarTheNukeHasNoEffect(int roll) {
        val raider = raiderAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(roll);

        execute(AT_4_OCLOCK);

        assertShipCount(GALACTICA_SPACE_4_OCLOCK, 1);
        assertEquals(GALACTICA_SPACE_4_OCLOCK, locate(raider));
    }

    @Test
    void theBasestarThereIsHit() {
        val basestar = basestarAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(4);

        execute(AT_4_OCLOCK);

        assertFollowup(new NukeBasestarEvent(1, basestar.id(), 4));
    }

    // With more than one basestar there, the player who launched the nuke chooses (rule 11.6.1).
    @Test
    void theLauncherChoosesTheBasestar() {
        val first = basestarAt(GALACTICA_SPACE_4_OCLOCK);
        val second = basestarAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(2);

        execute(AT_4_OCLOCK);

        assertFollowup(one(new NukeBasestarEvent(1, first.id(), 2), new NukeBasestarEvent(1, second.id(), 2)));
    }

    @Test
    void aSevenWithoutABasestarStillDestroysThreeRaiders() {
        val raiders = threeOfFourRaidersAt4Oclock();
        rolled(7);

        execute(AT_4_OCLOCK);

        assertFollowup(new DestroyRaidersEvent(raiders));
    }

    @Test
    void anEightDestroysEveryShipThere() {
        basestarAt(GALACTICA_SPACE_4_OCLOCK);
        raiderAt(GALACTICA_SPACE_4_OCLOCK);
        heavyRaiderAt(GALACTICA_SPACE_4_OCLOCK);
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        ops.player(2).pilot(viper.id());
        viperAt(GALACTICA_SPACE_4_OCLOCK);
        civilianShipAt(GALACTICA_SPACE_4_OCLOCK);
        val elsewhere = raiderAt(GALACTICA_SPACE_6_OCLOCK);
        rolled(8);

        execute(AT_4_OCLOCK);

        assertShipCount(GALACTICA_SPACE_4_OCLOCK, 0);
        assertEquals(GALACTICA_SPACE_6_OCLOCK, locate(elsewhere));
        assertEquals(SICKBAY, locate(KARA_STARBUCK_THRACE)); // the pilot of a destroyed viper
        assertEquals(2, game.removedComponents().items().stream()
                .filter(xyz.zlatanov.frakkintoasters.state.ship.Viper.class::isInstance).count());
        assertFollowup(new OpenInterruptWindowEvent(new AfterBasestarOrCenturionDestroyed()));
    }

    // Raiders are all alike: the ones with the lowest ids go.
    private Set<Integer> threeOfFourRaidersAt4Oclock() {
        return java.util.stream.Stream.generate(() -> raiderAt(GALACTICA_SPACE_4_OCLOCK).id())
                .limit(4)
                .sorted()
                .limit(3)
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void anEightWithoutABasestarOpensNoWindow() {
        raiderAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(8);

        execute(AT_4_OCLOCK);

        assertShipCount(GALACTICA_SPACE_4_OCLOCK, 0);
    }
}
