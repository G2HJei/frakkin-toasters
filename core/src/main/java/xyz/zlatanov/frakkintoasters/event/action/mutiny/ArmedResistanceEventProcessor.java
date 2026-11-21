package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.deck.LookAtTopCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.CRISIS;

public class ArmedResistanceEventProcessor extends MutinyCardEventProcessor<ArmedResistanceEvent> {

    @Override
    public MutinyCard card() {
        return MutinyCard.ARMED_RESISTANCE;
    }

    @Override
    protected Followup resolve() {
        return all(new SendToSickbayEvent(game.playerNumber(game.admiral())),
                new LookAtTopCardEvent(event.playerNumber(), CRISIS),
                thenDiscard());
    }
}
