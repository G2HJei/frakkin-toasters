package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;

// The Admiral places the card next to the Objective Card and follows its instructions, then the Objective Card's
// instructions, then the Jump Preparation track is reset (rule 12.1.1, steps 5 to 7).
public record ResolveDestinationCardEvent(DestinationCard card) implements Event {
}
