package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ResolveCapricaCrisisCardEvent;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class CapricaDrawAndResolveCrisisCardsEventProcessor extends LocationActionProcessor<CapricaDrawAndResolveCrisisCardsEvent> {


    @Override
    public Followup process() {
        val firstCrisis = ops.player(event.playerNumber()).drawCrisisCard();
        val secondCrisis = ops.player(event.playerNumber()).drawCrisisCard();
        return one(
                new ResolveCapricaCrisisCardEvent(event.playerNumber(), firstCrisis, secondCrisis),
                new ResolveCapricaCrisisCardEvent(event.playerNumber(), secondCrisis, firstCrisis)
        );
    }
}
