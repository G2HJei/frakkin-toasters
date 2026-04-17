package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

;

public record QuorumChamberActionEvent(int playerNumber) implements ActionEvent {

    @Override
    public List<Followup> apply(Game game) {
        val drawnCard = game.decks().quorum().draw();
        game.presidentHand().add(drawnCard);

        return followWith(one(
                new DrawQuorumCardEvent(),
                new PlayQuorumCardEvent()));
    }
}
