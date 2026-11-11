package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.IncreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.GameStep.PREPARE_FOR_JUMP;

public class PrepareForJumpStepEventProcessor extends EventProcessor<PrepareForJumpStepEvent> {

    @Override
    public Followup process() {
        // Not skipped on a revealed Cylon's turn: a Crisis Card chosen at "Caprica" counts too (rule 6.1.3.4).
        val followups = new ArrayList<Event>();
        game.turn().crisisCards().stream()
                .filter(CrisisCard::prepareForJump)
                .forEach(card -> followups.add(new IncreaseJumpPreparationEvent())); // one space per icon (rule 8.7)
        if (followups.isEmpty()) {
            // With the Kobol objective the step has nothing else to do, so it only happens for an icon (rule 8.1).
            return NONE;
        }
        ops.changeStep(PREPARE_FOR_JUMP);
        val current = game.currentPlayer();
        if (current.isMutineer()) {
            followups.add(new DrawMutinyCardEvent(current.number())); // once, for one or more icons (rule 8.7.1)
        }
        return all(followups.toArray(Event[]::new));
    }
}
