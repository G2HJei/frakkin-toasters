package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;
import static xyz.zlatanov.frakkintoasters.event.Followup.oneOf;

public record QuorumChamberActionEvent(int playerNumber) implements ActionEvent {

    @Override
    public void apply(Game game) {
        val drawnCard = game.decks().quorum().draw();
        game.presidentHand().add(drawnCard);
    }

    @Override
    public List<Followup> followup(Game game) {
        return followWith(
                oneOf(
                        new DrawQuorumCardEvent(),
                        new PlayQuorumCardEvent()
                ));
    }
}
