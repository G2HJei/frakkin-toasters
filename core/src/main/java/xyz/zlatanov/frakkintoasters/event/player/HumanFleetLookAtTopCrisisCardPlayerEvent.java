package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.decisionconstraint.Draw2SkillCards;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;

public record HumanFleetLookAtTopCrisisCardPlayerEvent(int playerNumber) implements PlayerEvent {

    @Override
    public List<Followup> apply(Game game) {
        val card = game.decks().crisis().draw();
        return followWith(
                one(new PlaceCrisisCardOnTopEvent(playerNumber, card),
                        new PlaceCrisisCardOnBottomEvent(playerNumber, card)),
                single(new PlayerDecisionEvent<>(playerNumber, ReceiveSkillCardsEvent.class, Draw2SkillCards.class))
        );
    }
}
