package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.deck.Discard1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.Collections;
import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class DrawMutinyCardEventProcessor extends EventProcessor<DrawMutinyCardEvent> {

    @Override
    public Followup process() {
        // Revealed Cylons never draw Mutiny Cards, and Cylon Leaders only while Infiltrating (rules 5.2.3.4 and 5.2.3.5).
        if (!player.isHuman()) {
            return NONE;
        }
        ops.player(player.number()).drawMutinyCard();
        // The second Mutiny Card sends a player to the "Brig", the third the Mutineer (rules 5.2.3.2 and 5.2.3.3).
        val cardsToKeep = player.isMutineer() ? 2 : 1;
        val extraCards = player.mutinyCards().size() - cardsToKeep;
        if (extraCards <= 0) {
            return NONE;
        }
        // In the "Brig" he keeps that many and discards the rest. So does a "Stranded" Karl "Helo" Agathon, who isn't
        // on the board and doesn't move (rule A2.1.1.10).
        val location = game.locate(player.character());
        val staysWhereHeIs = location.isEmpty() || location.equals(Optional.of(BRIG));
        return staysWhereHeIs
                ? discard(extraCards)
                : all(single(new SendToBrigEvent(player.number())), discard(extraCards));
    }

    private Followup discard(int cards) {
        // One card at a time, so the player chooses which ones he keeps.
        val decision = single(new PlayerDecisionEvent<>(player.number(), Discard1MutinyCardEvent.class));
        return all(Collections.nCopies(cards, decision).toArray(Followup[]::new));
    }
}
