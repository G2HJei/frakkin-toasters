package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;
import xyz.zlatanov.frakkintoasters.event.player.LookAtLoyaltyCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.ArrayList;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEventProcessor.canBecomePresident;
import static xyz.zlatanov.frakkintoasters.event.player.ExecuteEventProcessor.canBeExecuted;
import static xyz.zlatanov.frakkintoasters.event.player.SendToBrigEventProcessor.canBeSentToBrig;
import static xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEventProcessor.canBeChosenForSickbay;

public class ChooseCrisisTargetEventProcessor extends EventProcessor<ChooseCrisisTargetEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null && hasTargetChoice(event.card())
                && isBeingResolved(event.card())
                && event.playerNumber() == chooser(game, event.card())
                && legalTargets(game, event.card()).contains(event.targetPlayerNumber());
    }

    // The client names the card, so it could answer with another card the same title holder chooses for, e.g. "Requested
    // Resignation" instead of "The Circle". The card being resolved is the last one played into the turn: drawn in the
    // Crisis step or chosen at "Caprica".
    private boolean isBeingResolved(CrisisCard card) {
        val cards = game.turn().crisisCards();
        return !cards.isEmpty() && cards.getLast() == card;
    }

    @Override
    public Followup process() {
        val target = event.targetPlayerNumber();
        return switch (event.card()) {
            case AN_AMBITIOUS_OPERATION -> {
                ops.player(target).gainMiracleToken();
                yield Followup.NONE;
            }
            case SECRET_MEETINGS -> single(new DrawMutinyCardEvent(target));
            case INTERROGATION -> all(single(new SendToSickbayEvent(target)),
                    one(new LookAtLoyaltyCardEvent(event.playerNumber(), target), new NoOpEvent(event.playerNumber()))); // "may"
            case THE_CIRCLE -> target == null
                    ? single(new ExecuteEvent(game.turn().currentPlayerNumber()))
                    : single(new MakePresidentEvent(game.player(target).character()));
            case REQUESTED_RESIGNATION -> target == null
                    ? single(new SendToBrigEvent(event.playerNumber()))
                    : single(new MakePresidentEvent(game.player(target).character()));
            default -> throw new FrakCallTheAdmiralException();
        };
    }

    public static boolean hasTargetChoice(CrisisCard card) {
        return switch (card) {
            case AN_AMBITIOUS_OPERATION, SECRET_MEETINGS, INTERROGATION, THE_CIRCLE, REQUESTED_RESIGNATION -> true;
            default -> false;
        };
    }

    public static int chooser(GameView game, CrisisCard card) {
        return switch (card) {
            case AN_AMBITIOUS_OPERATION, INTERROGATION -> game.playerNumber(game.admiral());
            case SECRET_MEETINGS -> game.turn().currentPlayerNumber();
            case THE_CIRCLE, REQUESTED_RESIGNATION -> game.playerNumber(game.president());
            default -> throw new FrakCallTheAdmiralException();
        };
    }

    /**
     * @return the players the chooser can pick, and {@code null} when he can give the card's other answer. Empty when
     * there is nothing to choose, so no decision is asked
     */
    public static List<Integer> legalTargets(GameView game, CrisisCard card) {
        val chooser = chooser(game, card);
        val targets = new ArrayList<Integer>();
        switch (card) {
            // "another player", who can have a miracle token (rules 4.3.1 to 4.3.3)
            case AN_AMBITIOUS_OPERATION -> game.players().stream()
                    .filter(p -> p.number() != chooser && p.canGainMiracleToken())
                    .forEach(p -> targets.add(p.number()));
            case SECRET_MEETINGS -> game.players().forEach(p -> targets.add(p.number()));
            // "another player to send to Sickbay" (rule 9.3.2)
            case INTERROGATION -> game.players().stream()
                    .filter(p -> p.number() != chooser && canBeChosenForSickbay(game, p.number()))
                    .forEach(p -> targets.add(p.number()));
            case THE_CIRCLE -> {
                game.players().stream()
                        .filter(p -> canBecomePresident(game, p.character()))
                        .forEach(p -> targets.add(p.number()));
                if (canBeExecuted(game, game.turn().currentPlayerNumber())) {
                    targets.add(null);
                }
            }
            case REQUESTED_RESIGNATION -> {
                if (canBecomePresident(game, game.admiral())) {
                    targets.add(game.playerNumber(game.admiral()));
                }
                if (canBeSentToBrig(game, chooser)) {
                    targets.add(null);
                }
            }
            default -> throw new FrakCallTheAdmiralException();
        }
        return targets;
    }
}
