package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Execute the player's character, e.g. after passing the "Airlock" skill check (rule 7.1). The player of the executed
// character carries out the steps, so he is the player of the event.
// withoutMoraleLoss: the execution of a human costs no morale, as Anastasia "Dee" Dualla's "Emotionally Fragile" says.
public record ExecuteEvent(int playerNumber, boolean withoutMoraleLoss) implements PlayerEvent {

    public ExecuteEvent(int playerNumber) {
        this(playerNumber, false);
    }
}
