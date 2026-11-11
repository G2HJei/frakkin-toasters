package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class QuorumChamberEventProcessor extends LocationActionProcessor<QuorumChamberEvent> {

    // "If you are the President"
    public static boolean canAct(GameView game, PlayerView player) {
        return player.character() == game.president();
    }

    @Override
    public Followup process() {
        ops.titles().drawQuorumCard();

        return one(// todo this should be aligned to the actual location text
                new DrawQuorumCardEvent(),
                new PlayerDecisionEvent<>(event.playerNumber(), PlayQuorumCardEvent.class));
    }
}
