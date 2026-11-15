package xyz.zlatanov.frakkintoasters.event.interrupt;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterViperAttacked;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.event.interrupt.PlayInterruptCardEventProcessor.pilotedFighterArea;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EVASIVE_MANOEUVRES;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.RUN_INTERFERENCE;

public class CloseInterruptWindowEventProcessor extends EventProcessor<CloseInterruptWindowEvent> {

    private static final int STRATEGIC_PLANNING_BONUS   = 2;
    private static final int EVASIVE_MANOEUVRES_PENALTY = -2;
    private static final int RUN_INTERFERENCE_MISSES    = 4;

    @Override
    protected boolean isValid() {
        return game.turn().interruptWindow().isPresent();
    }

    @Override
    public Followup process() {
        // Everyone has answered, so the cards are revealed, and the current player settles the conflicts (rule 13.3).
        if (!conflicts(game).isEmpty()) {
            return all(
                    new PlayerDecisionEvent<>(game.turn().currentPlayerNumber(), TiebreakInterruptCardsEvent.class),
                    new CloseInterruptWindowEvent());
        }
        val window = game.turn().interruptWindow().orElseThrow();
        val effects = new ArrayList<Followup>();
        for (val card : game.turn().windowCards()) { // in the order they were played, clockwise from the current player
            ops.interrupts().resolve(card);
            effects.add(resolve(window, card));
        }
        ops.interrupts().closeWindow();
        return allOf(effects.toArray(Followup[]::new));
    }

    public static List<List<PlayedInterruptCard>> conflicts(GameView game) {
        return game.turn().windowCards().stream()
                .collect(groupingBy(card -> conflictKey(game, card), LinkedHashMap::new, toList()))
                .values().stream()
                .filter(group -> group.size() > 1)
                .toList();
    }

    private static Object conflictKey(GameView game, PlayedInterruptCard card) {
        // One card of a type stands in a window, e.g. "Limit of 1 "Strategic Planning" card used per die roll", except
        // "Run Interference", which covers the player's own space area.
        val type = card.card().type();
        return type == RUN_INTERFERENCE
                ? List.of(type, pilotedFighterArea(game, game.player(card.playerNumber())).orElseThrow())
                : type;
    }

    private Followup resolve(InterruptWindow window, PlayedInterruptCard card) {
        return switch (card.card().type()) {
            case STRATEGIC_PLANNING -> playOnDieRoll(card, STRATEGIC_PLANNING_BONUS);
            case CALCULATIONS -> playOnDieRoll(card, card.calculationsModifier());
            case BEST_OF_THE_BEST ->
                    playOnDieRoll(card, 0); // the attack's result destroys raiders instead of attacking
            case EVASIVE_MANOEUVRES -> evasiveManoeuvres((AfterViperAttacked) window);
            case RUN_INTERFERENCE -> runInterference(card);
            case MAJOR_VICTORY -> rollDie(new MajorVictoryResultEvent());
            default -> throw new FrakCallTheAdmiralException("Not an interrupt card: " + card);
        };
    }

    private Followup playOnDieRoll(PlayedInterruptCard card, int modifier) {
        ops.playOnDieRoll(card.card().type(), modifier);
        return NONE;
    }

    private Followup evasiveManoeuvres(AfterViperAttacked window) {
        val piloted = galacticaBoard.shipInSpace(window.humanFighterId(), HumanFighterView.class).pilot() != null;
        // "If the viper is piloted, subtract 2 from the new roll." A later reroll replaces the roll it was subtracted
        // from, so it is subtracted once.
        val subtracted = game.turn().dieRoll().cards().contains(EVASIVE_MANOEUVRES);
        ops.playOnDieRoll(EVASIVE_MANOEUVRES, piloted && !subtracted ? EVASIVE_MANOEUVRES_PENALTY : 0);
        return all(new RollDieEvent(), new OpenInterruptWindowEvent(window)); // the window opens again on the new roll
    }

    private Followup runInterference(PlayedInterruptCard card) {
        val area = pilotedFighterArea(game, game.player(card.playerNumber())).orElseThrow();
        val raiders = galacticaBoard.shipsIn(area, Raider.class).size();
        // The raiders activate next. Counting no more than are there leaves nothing over for a later activation.
        ops.interrupts().raidersWillMiss(area, Math.min(RUN_INTERFERENCE_MISSES, raiders));
        return NONE;
    }
}
