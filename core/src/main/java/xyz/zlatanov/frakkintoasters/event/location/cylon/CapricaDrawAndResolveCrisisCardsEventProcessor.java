package xyz.zlatanov.frakkintoasters.event.location.cylon;

import java.util.List;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
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
        ops.player(event.playerNumber()).see(DeckId.CRISIS, List.of(firstCrisis, secondCrisis));
        return one(
                new ResolveCapricaCrisisCardEvent(event.playerNumber(), firstCrisis, secondCrisis),
                new ResolveCapricaCrisisCardEvent(event.playerNumber(), secondCrisis, firstCrisis)
        );
    }
}
