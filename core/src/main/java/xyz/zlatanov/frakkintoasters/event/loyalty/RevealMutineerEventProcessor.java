package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.officials.GiveTitlesToNextInLineEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class RevealMutineerEventProcessor extends EventProcessor<RevealMutineerEvent> {

    @Override
    protected boolean isValid() {
        return player.character() != null && player.isHuman() && player.isMutineer();
    }

    @Override
    public Followup process() {
        // "Any time you receive this card, lose your titles and draw a Mutiny Card." The Mutineer drawing another
        // Loyalty Card comes first, so the player knows the new card before the Mutiny Card is drawn.
        val followups = new ArrayList<Event>();
        if (event.drawsLoyaltyCard()) {
            followups.add(new DealLoyaltyCardsEvent(player.number(), 1, true));
        }
        followups.add(new GiveTitlesToNextInLineEvent(player.number()));
        followups.add(new DrawMutinyCardEvent(player.number()));
        return all(followups.toArray(Event[]::new));
    }
}
