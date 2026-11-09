package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON;

public class StartTurnEventProcessor extends EventProcessor<StartTurnEvent> {

    @Override
    public Followup process() {
        ops.startTurn();
        placeStrandedHelo();
        return all(
                new ReceiveSkillsStepEvent(),
                new MovementStepEvent(),
                new ActionStepEvent(),
                new CrisisStepEvent(),
                new ActivateCylonShipsStepEvent(),
                new PrepareForJumpStepEvent(),
                new DiscardStepEvent(),
                new EndTurnEvent());
    }

    // Stranded: "At the start of your 2nd turn, place your character on the Hangar Deck location." His own turns,
    // because the turn number counts everyone's.
    // todo an execution restarts the count: a Helo selected then is Stranded for his player's next turn (rule A2.1.1.10)
    private void placeStrandedHelo() {
        val current = game.currentPlayer();
        if (current.character() == KARL_HELO_AGATHON
                && game.locate(KARL_HELO_AGATHON).isEmpty()
                && current.turns() == 2) {
            ops.player(current.number()).moveTo(HANGAR_DECK);
        }
    }
}
