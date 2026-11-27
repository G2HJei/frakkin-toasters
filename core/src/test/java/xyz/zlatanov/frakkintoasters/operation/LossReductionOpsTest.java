package xyz.zlatanov.frakkintoasters.operation;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.ResourceDecreased;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.LossReduced;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.LossReductionAdded;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_3_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.*;

/**
 * "Preventative Policy": every loss goes through {@code AbstractOps.loseResource}, which makes the next loss of the
 * resource this turn 1 smaller.
 */
class LossReductionOpsTest extends TestHarness {

    @Test
    void theNextLossIsOneSmaller() {
        ops.reduceNextLoss(FOOD);

        ops.galactica().decreaseResource(FOOD, 2);

        assertEquals(7, galacticaBoard.food());
        assertEquals(List.of(new LossReductionAdded(FOOD), new LossReduced(FOOD), new ResourceDecreased(FOOD, 1)),
                journal.facts());
        assertEquals(Set.of(), game.turn().lossReductions());
    }

    @Test
    void onlyTheNextLossIsReduced() {
        ops.reduceNextLoss(MORALE);

        ops.galactica().decreaseMorale(1);
        ops.galactica().decreaseMorale(1);

        assertEquals(9, galacticaBoard.morale());
    }

    @Test
    void anotherResourceIsNotReduced() {
        ops.reduceNextLoss(MORALE);

        ops.galactica().decreasePopulation(2);

        assertEquals(10, galacticaBoard.population());
        assertEquals(Set.of(MORALE), game.turn().lossReductions());
    }

    @Test
    void aDamageTokenIsALoss() {
        ops.reduceNextLoss(FUEL);
        galacticaDamage.nextCard(GalacticaDamage.FUEL);

        ops.galactica().damage();

        assertEquals(8, galacticaBoard.fuel());
    }

    // A destroyed civilian ship loses each of its resources (rule 13.2: a loss "for any reason").
    @Test
    void aDestroyedCivilianShipIsALoss() {
        ops.reduceNextLoss(POPULATION);
        civilianShips.nextCard(new CivilianShip(-1, 1, 0, 2));
        place(GALACTICA_SPACE_3_OCLOCK, civilianShip());

        ops.ship(-1).destroy();

        assertEquals(7, galacticaBoard.fuel());
        assertEquals(11, galacticaBoard.population());
    }

    // The sandbox sets a resource up: that is not a loss.
    @Test
    void theSandboxDoesNotUseTheReduction() {
        ops.reduceNextLoss(FOOD);

        ops.sandbox().decreaseResource(FOOD, 1);

        assertEquals(7, galacticaBoard.food());
        assertEquals(Set.of(FOOD), game.turn().lossReductions());
    }
}
