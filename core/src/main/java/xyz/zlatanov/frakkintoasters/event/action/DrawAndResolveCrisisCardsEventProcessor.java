package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ResolveCapricaCrisisCardEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class DrawAndResolveCrisisCardsEventProcessor extends EventProcessor<DrawAndResolveCrisisCardsEvent> {


    @Override
    public Followup process() {
        val firstCrisis = game.decks().crisis().draw();
        val secondCrisis = game.decks().crisis().draw();
        return one(
                new ResolveCapricaCrisisCardEvent(event.playerNumber(), firstCrisis, secondCrisis),
                new ResolveCapricaCrisisCardEvent(event.playerNumber(), secondCrisis, firstCrisis)
        );
    }
}
