package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.ResourceDecreased;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.DeckShuffled;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.CharacterMoved;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.*;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LOUANNE_KAT_KATRAINE;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.STRUCTURAL_DAMAGE;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipZone.*;

class ShipOpsJournalTest extends TestHarness {

    @Test
    void aHeavyRaiderBoardingTurnsIntoACenturionOnTheBoardingPartyTrack() {
        val heavyRaider = heavyRaiderAt(GALACTICA_SPACE_4_OCLOCK);
        val centurion = cylonShips.nextCenturion().orElseThrow();

        ops.ship(heavyRaider.id()).boardGalactica();

        assertEquals(List.of(
                new ShipMoved(heavyRaider.id(), GALACTICA_SPACE, CYLON_SUPPLY, null),
                new ShipMoved(centurion.id(), CENTURION_SUPPLY, BOARDING_PARTY, null)), journal.facts());
        assertTrue(galacticaBoard.boardingPartyTrack().containsKey(centurion));
    }

    @Test
    void destroyingACenturionReturnsItToTheCenturionSupply() {
        val centurion = cylonShips.centurion().orElseThrow();
        galacticaBoard.boardGalactica(centurion);

        ops.galactica().destroyCenturion(centurion.id());

        assertEquals(List.of(new ShipMoved(centurion.id(), BOARDING_PARTY, CENTURION_SUPPLY, null)), journal.facts());
        assertTrue(galacticaBoard.boardingPartyTrack().isEmpty());
        assertTrue(cylonShips.centurions().contains(centurion));
    }

    @Test
    void destroyingARaiderSendsItBackToTheSupply() {
        val raider = raiderAt(GALACTICA_SPACE_6_OCLOCK);

        ops.ship(raider.id()).destroy();

        assertEquals(List.of(new ShipMoved(raider.id(), GALACTICA_SPACE, CYLON_SUPPLY, null)), journal.facts());
    }

    @Test
    void destroyingADamagedBasestarReturnsItsDamageCardsBeforeTheShip() {
        val basestar = basestarAt(GALACTICA_SPACE_2_OCLOCK);
        basestar.damage(STRUCTURAL_DAMAGE);

        ops.ship(basestar.id()).destroy();

        val facts = journal.facts();
        assertEquals(new BasestarDamageCleared(basestar.id()), facts.get(0));
        assertEquals(new CardsAddedOnTop(DeckId.BASESTAR_DAMAGE, List.of(STRUCTURAL_DAMAGE)), facts.get(1));
        assertEquals(DeckId.BASESTAR_DAMAGE, ((DeckShuffled) facts.get(2)).deck());
        assertEquals(new ShipMoved(basestar.id(), GALACTICA_SPACE, CYLON_SUPPLY, null), facts.get(3));
        assertEquals(4, facts.size());
    }

    @Test
    void damagingAPilotedViperClearsThePilotAndSendsThemToSickbay() {
        val viper = viperAt(GALACTICA_SPACE_2_OCLOCK);
        game.player(1).character(LOUANNE_KAT_KATRAINE);
        viper.pilot(LOUANNE_KAT_KATRAINE);

        ops.ship(viper.id()).damage();

        assertEquals(List.of(
                new PilotChanged(viper.id(), null),
                new CharacterMoved(LOUANNE_KAT_KATRAINE, SICKBAY),
                new ShipMoved(viper.id(), GALACTICA_SPACE, DAMAGED, null)), journal.facts());
    }

    @Test
    void destroyingACivilianShipLogsTheResourcesLostAndTheRemoval() {
        val civ = civilianShipAt(GALACTICA_SPACE_2_OCLOCK);

        ops.ship(civ.id()).destroy();

        val expected = new java.util.ArrayList<xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact>();
        if (civ.fuelCost() > 0) expected.add(new ResourceDecreased(Resource.FUEL, civ.fuelCost()));
        if (civ.moraleCost() > 0) expected.add(new ResourceDecreased(Resource.MORALE, civ.moraleCost()));
        if (civ.populationCost() > 0) expected.add(new ResourceDecreased(Resource.POPULATION, civ.populationCost()));
        expected.add(new ShipMoved(civ.id(), GALACTICA_SPACE, REMOVED, null));
        assertEquals(expected, journal.facts());
    }
}
