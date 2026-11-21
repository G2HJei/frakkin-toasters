package xyz.zlatanov.frakkintoasters.engine.journal;

import java.util.List;
import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.Die;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforeRaidersActivate;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.ship.*;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.GameStep.CRISIS;
import static xyz.zlatanov.frakkintoasters.state.GameStep.RECEIVE_SKILLS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.STRATEGIC_PLANNING;

/**
 * A game that uses every kind of op: setup shuffles, draws, a skill check, ship damage and destruction, a Cylon attack,
 * a die roll. It records every kind of fact. One of its scopes has no event, so only {@link GameReplayer} can rebuild
 * it, not {@code GameEngine.restore}.
 */
public final class ScriptedScenario {

    private ScriptedScenario() {
    }

    public static Game play(Journal journal) {
        val game = Game.builder()
                .randomness(Randomness.seeded(7))
                .die(new Die(Randomness.seeded(8)))
                .build();
        val ops = new GameOps(game, new FactRecorder(game, journal));

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
        ops.titles().drawQuorumCard();
        ops.titles().discardQuorumCards(java.util.List.of(game.presidentHand().cards().getFirst()));
        val playedQuorumCard = game.presidentHand().cards().getFirst();
        ops.titles().play(playedQuorumCard);
        ops.titles().removeQuorumCardFromGame(playedQuorumCard);
        ops.loyaltyDeck().createNotACylon().create(2, 1, false);
        ops.player(1).drawLoyaltyCards(1);
        ops.skillCheck().createDestinyDeck();
        ops.galactica().setUp();
        ops.changeCurrentPlayer(1);
        ops.startTurn();
        ops.changeStep(RECEIVE_SKILLS);

        // draws
        ops.player(1).drawSkillCards(POLITICS, LEADERSHIP, TACTICS, PILOTING, ENGINEERING);
        ops.player(2).drawSkillCards(POLITICS, POLITICS, TACTICS, ENGINEERING, TREACHERY);
        ops.player(3).drawMotiveCards();
        ops.player(1).drawMutinyCard();
        ops.player(2).drawSuperCrisisCard();
        CrisisCard crisis = ops.player(1).drawCrisisCard();
        ops.player(1).placeCrisisCardOnBottom(crisis);
        ops.playCrisisCard(ops.player(1).drawCrisisCard());
        ops.playCrisisCard(ops.player(1).drawCrisisCard());
        ops.discardCrisisCards();
        ops.playCrisisCard(ops.player(1).drawCrisisCard()); // stays with the turn
        DestinationCard destination = ops.player(2).drawDestinationCard();
        ops.player(2).see(xyz.zlatanov.frakkintoasters.state.deck.DeckId.DESTINATION, List.of(destination));
        ops.sandbox().stackOnTop(xyz.zlatanov.frakkintoasters.state.deck.DeckId.CRISIS, game.decks().crisis().cards().get(3));
        ops.player(2).placeDestinationCardOnTop(destination);
        ops.player(2).discard(game.player(2).skillCards().cards().getFirst());
        ops.player(2).play(game.player(2).skillCards().cards().getFirst());
        ops.player(1).discard(game.player(1).mutinyCards().cards().getFirst());

        // what a Crisis Card leaves behind
        ops.keepCrisisCardInPlay(SkillCheckType.DETECTOR_SABOTAGE);
        ops.returnToCrisisStep(true);
        ops.discardNuke();
        ops.gainNuke();
        ops.moraleLostToExecution();
        ops.holdCylonShips();
        ops.reduceNextLoss(xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD);
        ops.galactica().decreaseResource(xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD, 1); // reduced to nothing
        ops.player(2).lookAtLoyaltyCard(1);

        // a skill check, in an event scope, then a choice the event queued
        val scope = journal.openScope(null, 1, Journal.ROOT_SCOPE);
        ops.skillCheck().start(SkillCheckType.values()[0], 2);
        val played = game.player(1).skillCards().cards().getFirst();
        ops.skillCheck().playBeforeSkillCheck(1, java.util.List.of(played));
        ops.skillCheck().revealPlayedBefore();
        val shown = List.of(game.player(2).skillCards().cards().get(0), game.player(2).skillCards().cards().get(1));
        ops.skillCheck().playCards(2, shown);
        ops.skillCheck().show(2, shown);
        ops.skillCheck().playFromDestinyDeck(2);
        ops.skillCheck().playFromDestinyDeck(1, true);
        ops.skillCheck().shuffleAndDivideCards();
        ops.skillCheck().resolveAbility(xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.IRON_WILL);
        ops.skillCheck().declareEmergency();
        ops.skillCheck().totalStrength(5);
        ops.skillCheck().determineResult(xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS);
        ops.skillCheck().replacePassResult();
        val inPile = game.activeSkillCheck().matchingPile().isEmpty()
                ? game.activeSkillCheck().nonMatchingPile().cards().getFirst()
                : game.activeSkillCheck().matchingPile().cards().getFirst();
        ops.skillCheck().discard(inPile);
        ops.skillCheck().end(); // discards the card played before it and what is left in the piles
        val option = single(new NoOpEvent(1));
        journal.closeScope(one(option, NONE));
        journal.recordChoice(scope, 1, option);
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
        ops.galactica().buildAssaultRaptor();
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
        ops.player(2).lookAtShip(civilians.get(1));
        ops.ship(civilians.get(1)).escort(); // player 2 forgets it
        ops.galactica().placeCivilianShip(GALACTICA_SPACE_6_OCLOCK);
        ops.galactica().placeRaider(GALACTICA_SPACE_6_OCLOCK);
        ops.ship(basestar).destroy();

        // the boards and resources
        for (int i = 0; i < 4; i++) {
            ops.galactica().damage();
        }
        ops.pegasus().damage();
        ops.pegasus().damage();
        ops.pegasus().repairLocation(game.boards().pegasus().damagedLocations().iterator().next());
        ops.pegasus().activateEngineRoom();
        ops.galactica().decreasePopulation(2);
        ops.galactica().decreaseMorale(1);
        ops.galactica().decreaseResource(Resource.FUEL, 1);
        ops.galactica().increaseResource(Resource.FOOD, 2);
        ops.galactica().increaseJumpPreparation();
        ops.galactica().increaseJumpPreparation();
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
        ops.startDieRoll();
        ops.rollDie();
        ops.resetActingPlayer();
        ops.playOnDieRoll(STRATEGIC_PLANNING, 2);
        ops.rollDie();
        ops.rollDie();
        ops.changeStep(CRISIS);

        // an interrupt window: one card goes back to the hand, the other takes effect
        val hand = game.player(1).skillCards().cards();
        val returned = new PlayedInterruptCard(1, hand.get(0), null);
        val resolved = new PlayedInterruptCard(1, hand.get(1), null);
        ops.interrupts().openWindow(new BeforeRaidersActivate());
        ops.interrupts().play(returned);
        ops.interrupts().play(resolved);
        ops.interrupts().returnToHand(returned);
        ops.interrupts().resolve(resolved);
        ops.interrupts().closeWindow();
        ops.interrupts().raidersWillMiss(GALACTICA_SPACE_6_OCLOCK, 2);
        ops.interrupts().raiderMissed(GALACTICA_SPACE_6_OCLOCK);

        // an execution's facts
        ops.player(3).endInfiltration();
        ops.player(1).loseMiracleToken();
        ops.player(1).revealLoyaltyCard(game.player(1).loyaltyCards().cards().getFirst());
        ops.player(3).revealMotiveCard(game.player(3).motiveCards().cards().getFirst());
        ops.player(2).removeCharacter();
        ops.endTurnEarly();

        // a jump: the fleet reaches a destination and the Jump Preparation track is reset
        ops.galactica().reachDestination(DestinationCard.DEEP_SPACE);
        ops.galactica().resetJumpPreparation();
        ops.endGame(Team.CYLONS, List.of());

        return game;
    }
}
