package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.crisis.DiscardSuperCrisisCardEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ResolveSuperCrisisCardEvent;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class CapricaPlaySuperCrisisCardEventProcessor extends LocationActionProcessor<CapricaPlaySuperCrisisCardEvent> {

    @Override
    protected boolean isValidAction() {
        return event.superCrisisCard() != null && player.superCrisisCards().cards().contains(event.superCrisisCard());
    }

    // The card leaves the hand, resolves like a Crisis Card (rule 6.1.1, step 4), and goes to the discard pile unless it
    // stays in play.
    @Override
    protected Followup act() {
        ops.player(player.number()).playSuperCrisisCard(event.superCrisisCard());
        return all(new ResolveSuperCrisisCardEvent(player.number(), event.superCrisisCard()),
                new DiscardSuperCrisisCardEvent(event.superCrisisCard()));
    }
}
