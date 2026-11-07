package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.DecreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.event.player.DiscardAllSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageGalacticaEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyRaptorEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.*;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.*;

/**
 * Builds the followups of a choice crisis (rule 8.5.2)
 */
@RequiredArgsConstructor
public class CrisisCardFollowups {

    private final GameView game;


    public Followup choice(CrisisCard card) {
        val chooser = chooser(card);
        return one(new ChooseCrisisSectionEvent(chooser, card, TOP), new ChooseCrisisSectionEvent(chooser, card, BOTTOM));
    }

    public Followup followup(CrisisCard card, Section section) {
        return switch (section) {
            case TOP -> top(card);
            case BOTTOM -> bottom(card);
        };
    }

    private Followup top(CrisisCard card) {
        return switch (card) {
            // resources only
            case AMBUSHED_BY_THE_PRESS, CONTROVERSIAL_MANUSCRIPT, QUESTION_PROCEDURE -> single(lose(MORALE, 1));
            case LABOR_DISPUTE -> single(lose(MORALE, 2));
            case WATER_SHORTAGE_BASESTAR_AND_JUMP, WATER_SHORTAGE_BASESTAR_NO_JUMP, WATER_SHORTAGE_RAIDERS ->
                    single(lose(FOOD, 1));
            case FOOD_SHORTAGE, WIDESPREAD_STARVATION -> single(lose(FOOD, 2));
            case RIOTS_JUMP, RIOTS_NO_JUMP -> all(lose(FOOD, 1), lose(MORALE, 1));
            case RESCUE_CAPRICA_SURVIVORS -> all(lose(FUEL, 1), lose(FOOD, 1), gain(POPULATION, 1));
            // galactica
            case RAIDERS_INBOUND -> all(lose(POPULATION, 1), new DamageGalacticaEvent());
            // sickbay
            case RESCUE_MISSION_BASESTAR, RESCUE_MISSION_RAIDER ->
                    allOf(single(lose(MORALE, 1)), onCurrentPlayer(new SendToSickbayEvent(currentPlayer())));
            case HIDDEN_EXPLOSIVES ->
                    allOf(single(new DestroyRaptorEvent()), onCurrentPlayer(new SendToSickbayEvent(currentPlayer())));
            default -> throw new FrakCallTheAdmiralException(); //todo the other crisis cards
        };
    }

    private Followup bottom(CrisisCard card) {
        return switch (card) {
            // resources only
            case HIDDEN_EXPLOSIVES, RESCUE_CAPRICA_SURVIVORS -> single(lose(MORALE, 1));
            case RIOTS_JUMP, RIOTS_NO_JUMP -> all(lose(POPULATION, 1), lose(FUEL, 1));
            case WIDESPREAD_STARVATION -> all(lose(FOOD, 1), lose(POPULATION, 1));
            // galactica and jump preparation
            case CONTROVERSIAL_MANUSCRIPT ->
                    all(gain(MORALE, 1), new DamageGalacticaEvent(), new DamageGalacticaEvent());
            case LABOR_DISPUTE -> all(lose(FUEL, 1), new DecreaseJumpPreparationEvent());
            // ships
            case RESCUE_MISSION_BASESTAR, RESCUE_MISSION_RAIDER -> all(lose(FUEL, 1), new DestroyRaptorEvent());
            // skill cards
            case AMBUSHED_BY_THE_PRESS -> single(new DiscardAllSkillCardsEvent(president()));
            case QUESTION_PROCEDURE -> all(new DamageGalacticaEvent(), discard(president(), 3));
            case RAIDERS_INBOUND -> all(discard(cag(), 3), discard(admiral(), 3));
            case WATER_SHORTAGE_BASESTAR_AND_JUMP, WATER_SHORTAGE_BASESTAR_NO_JUMP, WATER_SHORTAGE_RAIDERS ->
                    presidentThenCurrentPlayerDiscard();
            case FOOD_SHORTAGE -> allOf(single(lose(FOOD, 1)), presidentThenCurrentPlayerDiscard());
            default -> throw new FrakCallTheAdmiralException(); //todo the other crisis cards
        };
    }

    /* players */

    private int chooser(CrisisCard card) {
        return switch (card.chooser()) {
            case PRESIDENT -> president();
            case ADMIRAL -> admiral();
            case CAG -> cag();
            case null -> throw new FrakCallTheAdmiralException(); //todo the other crisis cards
        };
    }

    private int currentPlayer() {
        return game.turn().currentPlayerNumber();
    }

    private int president() {
        return game.playerNumber(game.president());
    }

    private int admiral() {
        return game.playerNumber(game.admiral());
    }

    private int cag() {
        return game.playerNumber(game.cag());
    }

    /* events */

    // A revealed Cylon ignores what a crisis does to them (rule 6.1.2.1). Only the current player can be one, when
    // resolving a card at "Caprica": nobody who holds a title is a revealed Cylon.
    private Followup onCurrentPlayer(Event effect) {
        return game.currentPlayer().isHuman() ? single(effect) : NONE;
    }

    // A President who is also the current player discards 5 Skill Cards (rule A2.3.9.1).
    private Followup presidentThenCurrentPlayerDiscard() {
        return allOf(single(discard(president(), 2)), onCurrentPlayer(discard(currentPlayer(), 3)));
    }
}
