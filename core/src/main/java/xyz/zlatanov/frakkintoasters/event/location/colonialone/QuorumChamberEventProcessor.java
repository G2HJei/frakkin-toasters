package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardPlays;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class QuorumChamberEventProcessor extends LocationActionProcessor<QuorumChamberEvent> {

    // "If you are the President"
    public static boolean canAct(GameView game, PlayerView player) {
        return player.character() == game.president();
    }

    @Override
    public Followup process() {
        if (game.decks().quorumCardsLeft() > 0) {
            ops.titles().drawQuorumCard();
        }
        // "You may then" draw another card or play one from the hand, the one just drawn included.
        val options = new ArrayList<Event>();
        if (game.decks().quorumCardsLeft() > 0) {
            options.add(new DrawQuorumCardEvent());
        }
        if (QuorumCardPlays.canPlayAny(game, player)) {
            options.add(new PlayerDecisionEvent<>(player.number(), QuorumCardEvent.class));
        }
        if (options.isEmpty()) {
            return NONE;
        }
        options.add(new NoOpEvent(player.number()));
        return one(options.toArray(Event[]::new));
    }
}
