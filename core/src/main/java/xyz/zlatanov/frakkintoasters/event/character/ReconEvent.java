package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.RECON;

// Sharon "Boomer" Valerii's "Recon": at the end of her turn she may look at the top Crisis Card and leave it there or put
// it at the bottom.
@Source(ability = RECON)
public record ReconEvent(int playerNumber) implements PlayerEvent {
}
