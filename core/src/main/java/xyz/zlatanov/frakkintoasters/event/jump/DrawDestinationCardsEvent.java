package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.event.Event;

// "The Admiral draws two cards from the Destination deck, chooses one, and places the other on the bottom of the deck
// without showing it to the other players." (rule 12.1.1, step 4)
public record DrawDestinationCardsEvent() implements Event {
}
