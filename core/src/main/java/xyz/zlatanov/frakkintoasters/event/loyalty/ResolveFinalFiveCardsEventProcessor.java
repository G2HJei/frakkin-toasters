package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ResolveFinalFiveCardsEventProcessor extends EventProcessor<ResolveFinalFiveCardsEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.holderPlayerNumber()) && isPlayer(event.lookerPlayerNumber())
                && event.cards() != null && !event.cards().isEmpty()
                && event.cards().stream().allMatch(card -> card != null && card.isFinalFive());
    }

    @Override
    public Followup process() {
        return FinalFiveCards.inOrder(event.holderPlayerNumber(), event.cards(), event.lookerPlayerNumber());
    }
}
