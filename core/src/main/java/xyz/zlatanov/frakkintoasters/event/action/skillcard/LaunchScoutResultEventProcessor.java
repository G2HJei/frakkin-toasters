package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.character.RaptorPilotEvent;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.deck.LookAtTopCardEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyRaptorEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.RAPTOR_PILOT;
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
        if (game.player(n).hasAbility(RAPTOR_PILOT)) {
            return one(new RaptorPilotEvent(n, CRISIS), new RaptorPilotEvent(n, DESTINATION)); // the top 2 cards
        }
        return one(new LookAtTopCardEvent(n, CRISIS), new LookAtTopCardEvent(n, DESTINATION));
    }
}
