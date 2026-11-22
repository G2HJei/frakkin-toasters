package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// A player chooses a character to send to the "Brig" (rule 9.2.3), the answer to a decision. The move is then a
// SendToBrigEvent, which an effect naming the character queues directly.
public record ChooseCharacterForBrigEvent(int playerNumber, int targetPlayerNumber) implements PlayerEvent {
}
