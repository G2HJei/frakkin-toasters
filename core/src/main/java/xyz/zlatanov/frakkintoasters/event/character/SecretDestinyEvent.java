package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.SECRET_DESTINY;

// Kara "Starbuck" Thrace's "Secret Destiny" (once per game): right after a Crisis Card is revealed, it is discarded and
// she draws a new one, which resolves instead.
@Source(ability = SECRET_DESTINY)
public record SecretDestinyEvent(int playerNumber, CrisisCard card) implements PlayerEvent {
}
