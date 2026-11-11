package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.deck.Discard1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.deck.DiscardDownTo1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;

public class PressRoomEventProcessor extends LocationActionProcessor<PressRoomEvent> {

    public static boolean canAct(GameView game, PlayerView player) {
        return game.players().stream().anyMatch(other -> canBeChosen(player, other));
    }

    @Override
    protected boolean isValidAction() {
        return isPlayer(event.targetPlayer()) && canBeChosen(player, game.player(event.targetPlayer()));
    }

    // Another player, and not a revealed Cylon, who can't be chosen to draw Mutiny Cards (rule 5.2.3.4).
    private static boolean canBeChosen(PlayerView player, PlayerView other) {
        return other.number() != player.number() && other.isHuman();
    }

    @Override
    public Followup process() {
        ops.player(event.targetPlayer()).drawMutinyCard(); // not a DrawMutinyCardEvent, he doesn't move to the "Brig"
        return allOf(keepOneMutinyCard(), mayDiscardAMutinyCard());
    }

    private Followup keepOneMutinyCard() {
        return game.player(event.targetPlayer()).mutinyCards().size() > 1
                ? single(new PlayerDecisionEvent<>(event.targetPlayer(), DiscardDownTo1MutinyCardEvent.class))
                : NONE;
    }

    private Followup mayDiscardAMutinyCard() {
        return player.mutinyCards().isEmpty()
                ? NONE
                : one(new PlayerDecisionEvent<>(event.playerNumber(), Discard1MutinyCardEvent.class),
                        new NoOpEvent(event.playerNumber()));
    }
}
