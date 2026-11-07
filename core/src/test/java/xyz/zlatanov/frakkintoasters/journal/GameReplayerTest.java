package xyz.zlatanov.frakkintoasters.journal;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.Die;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.ship.*;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.GameStep.CRISIS;
import static xyz.zlatanov.frakkintoasters.state.GameStep.RECEIVE_SKILLS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;

class GameReplayerTest {

    @Test
    void replayedGameEqualsTheLiveGame() {
        val live = playScenario();
        val replayed = GameReplayer.replay(live.journal());
        assertEquals(GameSnapshot.of(live), GameSnapshot.of(replayed));
    }

    @Test
    void replayDoesNotDependOnTheRandomnessOfTheReplayingGame() {
        val live = playScenario();

        val first = GameReplayer.replay(live.journal(), Randomness.seeded(1));
        val second = GameReplayer.replay(live.journal(), Randomness.seeded(2));

        assertEquals(GameSnapshot.of(live), GameSnapshot.of(first));
        assertEquals(GameSnapshot.of(live), GameSnapshot.of(second));
    }

    @Test
    void replayIgnoresScopes() {
        val live = playScenario();
        val scopes = live.journal().entries().stream()
                .filter(e -> !(e instanceof JournalEntry.FactRecorded))
                .count();

        assertTrue(scopes > 0);
        assertEquals(GameSnapshot.of(live), GameSnapshot.of(GameReplayer.replay(live.journal())));
    }

    @Test
    void aJournalHasToStartWithGameStarted() {
        assertThrows(IllegalArgumentException.class, () -> GameReplayer.replay(new Journal()));

        val journal = new Journal();
        journal.record(new xyz.zlatanov.frakkintoasters.journal.fact.GameFact.TurnStarted(1));

        assertThrows(IllegalArgumentException.class, () -> GameReplayer.replay(journal));
    }

    @Test
    void theSnapshotSeesTheDifferencesThatReplayHasToGetRight() {
        val live = playScenario();
        val snapshot = GameSnapshot.of(live);

        val replayed = GameReplayer.replay(live.journal());
        replayed.boards().galactica().decreaseFuel(1);
        assertNotEquals(snapshot, GameSnapshot.of(replayed));

        val replayed2 = GameReplayer.replay(live.journal());
        val basestar = replayed2.boards().galactica().shipsInSpace(Basestar.class).getFirst();
        basestar.clearDamage(); // a Basestar equals by id only, the snapshot has to look at its damage
        assertNotEquals(snapshot, GameSnapshot.of(replayed2));

        val replayed3 = GameReplayer.replay(live.journal());
        replayed3.boards().galactica().shipsInSpace(HumanFighter.class).forEach(f -> f.pilot(FELIX_GAETA));
        assertNotEquals(snapshot, GameSnapshot.of(replayed3));
    }

    @Test
    void theScenarioRecordsEveryKindOfFact() {
        val live = playScenario();
        val recorded = live.journal().facts().stream().map(Object::getClass).collect(java.util.stream.Collectors.toSet());

        val missing = java.util.Arrays.stream(Fact.class.getPermittedSubclasses())
                .flatMap(area -> java.util.Arrays.stream(area.getPermittedSubclasses()))
                .filter(type -> !recorded.contains(type))
                .map(Class::getSimpleName)
                .sorted()
                .toList();

        assertEquals(java.util.List.of(), missing);
    }

    /**
     * A game that uses every kind of op: setup shuffles, draws, a skill check, ship damage and destruction, a Cylon
     * attack, a die roll.
     */
    private Game playScenario() {
        val game = Game.builder()
                .randomness(Randomness.seeded(7))
                .die(new Die(Randomness.seeded(8)))
                .build();
        val ops = new GameOps(game);
        val journal = game.journal();

        // setup
        ops.startGame();
        ops.player(1).selectCharacter(LOUANNE_KAT_KATRAINE);
        ops.player(2).selectCharacter(FELIX_GAETA);
        ops.player(3).selectCharacter(AARON_DORAL);
        ops.player(1).moveTo(HANGAR_DECK);
        ops.player(2).moveTo(FTL_CONTROL);
        ops.player(3).moveTo(CAPRICA);
        ops.titles().assignPresident(FELIX_GAETA);
        ops.titles().assignAdmiral(FELIX_GAETA);
        ops.titles().assignCag(LOUANNE_KAT_KATRAINE);
        ops.titles().drawQuorumCard();
        ops.loyaltyDeck().createNotACylon().create(2, 1, false);
        ops.player(1).drawLoyaltyCards(1);
        ops.skillCheck().createDestinyDeck();
        ops.galactica().setUp();
        ops.changeStep(RECEIVE_SKILLS);
        ops.startTurn(1);
        ops.changeCurrentPlayer(1);

        // draws
        ops.player(1).drawSkillCards(POLITICS, LEADERSHIP, TACTICS, PILOTING, ENGINEERING);
        ops.player(2).drawSkillCards(POLITICS, POLITICS, TACTICS, ENGINEERING, TREACHERY);
        ops.player(3).drawMotiveCards();
        ops.player(1).drawMutinyCard();
        ops.player(2).drawSuperCrisisCard();
        CrisisCard crisis = ops.player(1).drawCrisisCard();
        ops.player(1).placeCrisisCardOnBottom(crisis);
        DestinationCard destination = ops.player(2).drawDestinationCard();
        ops.player(2).placeDestinationCardOnTop(destination);
        ops.player(2).discard(game.player(2).skillCards().cards().getFirst());
        ops.player(1).discard(game.player(1).mutinyCards().cards().getFirst());

        // a skill check, in an event scope
        journal.openScope(null, 1);
        ops.skillCheck().start(SkillCheckType.values()[0], 2);
        val played = game.player(1).skillCards().cards().getFirst();
        ops.skillCheck().playBeforeSkillCheck(1, java.util.List.of(played));
        ops.skillCheck().revealPlayedBefore();
        ops.skillCheck().playCards(2, game.player(2).skillCards().cards().get(0), game.player(2).skillCards().cards().get(1));
        ops.skillCheck().playFromDestinyDeck(2);
        ops.skillCheck().playFromDestinyDeck(1, true);
        ops.skillCheck().shuffleAndDivideCards();
        ops.skillCheck().declareEmergency();
        ops.skillCheck().totalStrength(5);
        ops.skillCheck().determineResult(xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS);
        ops.skillCheck().replacePassResult();
        val inPile = game.activeSkillCheck().matchingPile().isEmpty()
                ? game.activeSkillCheck().nonMatchingPile().cards().getFirst()
                : game.activeSkillCheck().matchingPile().cards().getFirst();
        ops.skillCheck().discard(inPile);
        journal.closeScope();
        ops.skillCheck().start(SkillCheckType.values()[1]); // a second check, that fails and is prevented
        ops.skillCheck().determineResult(xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.FAIL);
        ops.skillCheck().preventFailResult();
        ops.skillCheck().revealAndDiscardTreacheryCard();
        ops.skillCheck().shuffleIntoDestinyDeck(TREACHERY, TREACHERY, POLITICS);

        // ships
        val galactica = game.boards().galactica();
        val viperIds = galactica.shipsInSpace(Viper.class).stream().map(Ship::id).sorted().toList();
        ops.player(1).pilot(viperIds.get(0));
        ops.ship(viperIds.get(0)).moveTo(GALACTICA_SPACE_10_OCLOCK);
        ops.ship(viperIds.get(0)).damage();
        val launched = ops.galactica().launchHumanFighter(ShipType.VIPER, GALACTICA_SPACE_12_OCLOCK, null);
        ops.ship(launched).destroy();
        ops.player(2).pilot(viperIds.get(1));
        ops.ship(viperIds.get(1)).land(HANGAR_DECK);
        val basestar = galactica.shipsInSpace(Basestar.class).getFirst().id();
        ops.ship(basestar).damage();
        ops.ship(basestar).damage();
        ops.ship(basestar).launchRaiders(3);
        ops.ship(basestar).launchHeavyRaider();
        val raider = galactica.shipsInSpace(Raider.class).getFirst().id();
        ops.ship(raider).moveTo(GALACTICA_SPACE_2_OCLOCK);
        ops.ship(raider).destroy();
        val heavyRaider = galactica.shipsInSpace(HeavyRaider.class).getFirst().id();
        ops.ship(heavyRaider).boardGalactica();
        ops.galactica().advanceBoardingParty();
        val civilians = galactica.shipsInSpace(CivilianShip.class).stream().map(Ship::id).sorted().toList();
        ops.ship(civilians.get(0)).destroy();
        ops.ship(civilians.get(1)).escort();
        ops.galactica().placeCivilianShip(GALACTICA_SPACE_6_OCLOCK);
        ops.galactica().placeRaider(GALACTICA_SPACE_6_OCLOCK);
        ops.ship(basestar).destroy();

        // the boards and resources
        for (int i = 0; i < 4; i++) {
            ops.galactica().damage();
        }
        ops.pegasus().damage();
        ops.pegasus().activateEngineRoom();
        ops.galactica().decreasePopulation(2);
        ops.galactica().decreaseMorale(1);
        ops.galactica().decreaseResource(Resource.FUEL, 1);
        ops.galactica().increaseResource(Resource.FOOD, 2);
        ops.galactica().decreaseJumpPreparation();
        ops.cylonFleet().place(ShipType.RAIDER, CYLON_FLEET_SPACE_1);
        ops.cylonFleet().place(ShipType.BASESTAR, CYLON_FLEET_SPACE_2);
        ops.cylonFleet().advancePursuit();
        ops.cylonFleet().advancePursuit();
        ops.cylonFleet().moveShipsToGalactica(ShipType.RAIDER, CYLON_FLEET_SPACE_1);
        ops.cylonFleet().resetPursuit();
        ops.cylonFleet().advancePursuit();
        ops.cylonFleet().moveShipsToGalactica(ShipType.BASESTAR, CYLON_FLEET_SPACE_2);
        ops.ship(galactica.shipsInSpace(Basestar.class).getFirst().id()).damage(); // stays alive with its damage
        val newViper = ops.galactica().launchHumanFighter(ShipType.VIPER, GALACTICA_SPACE_6_OCLOCK, null);
        ops.player(1).pilot(newViper); // stays alive with its pilot

        // players
        ops.player(3).infiltrateGalactica(BRIG);
        ops.player(2).lookAtShip(2005);
        ops.player(2).gainMiracleToken();
        ops.player(2).sendTo(SICKBAY);
        ops.actingPlayer(3);
        ops.rollDie();
        ops.resetActingPlayer();
        ops.rollDie();
        ops.rollDie();
        ops.changeStep(CRISIS);

        return game;
    }
}
