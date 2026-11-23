package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.CHOOSE_A_DIFFERENT_PATH;

// Lee Adama's "Choose a Different Path" (once per game): when he has to choose on a Crisis Card, the result may instead
// be that the current player discards 5 Skill Cards.
@Source(ability = CHOOSE_A_DIFFERENT_PATH)
public record ChooseADifferentPathEvent(int playerNumber, CrisisCard card) implements PlayerEvent {
}
