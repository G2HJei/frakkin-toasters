package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.crisis.ResolveCrisisCardEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class ResolveCapricaCrisisCardEventProcessor extends EventProcessor<ResolveCapricaCrisisCardEvent> {

    @Override
    protected boolean isValid() {
        return event.crisisCardToPlay() != null && event.crisisCardToPutAtBottom() != null;
    }

    @Override
    public Followup process() {
        // The card counts for Prepare for Jump even on a revealed Cylon's turn (rule 6.1.3.4), so it goes into the turn
        // like a card of the Crisis step. There is no Activate Cylon Ships step.
        ops.playCrisisCard(event.crisisCardToPlay());
        ops.player(player.number()).placeCrisisCardOnBottom(event.crisisCardToPutAtBottom());
        return single(new ResolveCrisisCardEvent(event.crisisCardToPlay()));
    }
}
