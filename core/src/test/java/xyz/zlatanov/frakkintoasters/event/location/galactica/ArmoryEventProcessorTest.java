package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Centurion;
import xyz.zlatanov.frakkintoasters.state.track.BoardingParty;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ARMORY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAMUEL_T_ANDERS;

class ArmoryEventProcessorTest extends EventTestHarness<ArmoryEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(SAMUEL_T_ANDERS);
        moveTo(ARMORY, SAMUEL_T_ANDERS);
    }

    Centurion centurion;
    int       centurionId;

    @BeforeEach
    void setUp() {
        centurion = centurion();
        centurionId = centurion.id();
        galacticaBoard.boardGalactica(centurion);
    }

    public static Stream<Arguments> shouldAttackCenturion() {
        return Stream.of(
                argumentSet("Should do nothing on low roll",
                        6, BoardingParty.START, 1),
                argumentSet("Should destroy centurion on high roll",
                        7, null, 0)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldAttackCenturion(int dieRoll, BoardingParty centurionPosition, int centurionsOnBoard) {
        nextRoll(dieRoll);
        execute(new ArmoryEvent(1, centurionId));
        assertResult(centurionPosition, centurionsOnBoard);
    }

    @Test
    void shouldDestroySelectedCenturion() {
        val secondCenturion = centurion();
        val secondCenturionId = secondCenturion.id();
        galacticaBoard.boardGalactica(secondCenturion);
        nextRoll(8);

        execute(new ArmoryEvent(1, secondCenturionId));

        assertResult(BoardingParty.START, 1);
    }

    void assertResult(BoardingParty centurionPosition, int centurionsOnBoard) {
        assertEquals(centurionPosition, galacticaBoard.boardingPartyTrack().get(centurion));
        assertEquals(4 - centurionsOnBoard, cylonShips.centurions().size());
    }

    @Test
    void shouldAttackACenturionOnTheBoardingPartyTrack() {
        assertInvalid(new ArmoryEvent(1, centurion().id()));
    }
}
