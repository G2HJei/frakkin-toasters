package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.ABUSE_POWER;

// The Mutiny Card Tom Zarek (alt.) plays after "Abuse Power", and the others, which go to the bottom.
@Source(ability = ABUSE_POWER)
public record AbusePowerChoiceEvent(int playerNumber, MutinyCard chosen, List<MutinyCard> others) implements PlayerEvent {
}
