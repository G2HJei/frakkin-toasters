package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.deck.LookAtTopCardEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyRaptorEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.CRISIS;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.DESTINATION;

public class LaunchScoutResultEventProcessor extends EventProcessor<LaunchScoutResultEvent> {

    private static final int LOWEST_ROLL_TO_SCOUT = 3;

    @Override
    protected boolean isValid() {
        return isPlayer(event.playerNumber());
    }

    @Override
    public Followup process() {
        if (game.turn().dieRoll().result() < LOWEST_ROLL_TO_SCOUT) {
            return single(new DestroyRaptorEvent());
        }
        int n = event.playerNumber();
        return one(new LookAtTopCardEvent(n, CRISIS), new LookAtTopCardEvent(n, DESTINATION));
    }
}
