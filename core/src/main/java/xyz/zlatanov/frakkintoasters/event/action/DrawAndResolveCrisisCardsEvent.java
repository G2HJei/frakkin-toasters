package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.ResolveCapricaCrisisCardEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public record DrawAndResolveCrisisCardsEvent(int playerNumber) implements PlayerEvent {

    @Override
    public Followup apply(Game game) {
        val firstCrisis = game.decks().crisis().draw();
        val secondCrisis = game.decks().crisis().draw();
        return one(
                new ResolveCapricaCrisisCardEvent(playerNumber, firstCrisis, secondCrisis),
                new ResolveCapricaCrisisCardEvent(playerNumber, secondCrisis, firstCrisis)
        );
    }
}
