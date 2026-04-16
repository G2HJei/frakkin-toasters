package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.ResolveCapricaCrisisCardEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public record CapricaActionOption2Event(int playerNumber) implements PlayerEvent {

    @Override
    public List<Followup> apply(Game game) {
        val firstCrisis = game.decks().crisis().draw();
        val secondCrisis = game.decks().crisis().draw();
        return followWith(one(
                        new ResolveCapricaCrisisCardEvent(playerNumber, firstCrisis, secondCrisis),
                        new ResolveCapricaCrisisCardEvent(playerNumber, secondCrisis, firstCrisis)
                )
        );
    }
}
