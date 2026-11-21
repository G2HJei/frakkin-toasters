package xyz.zlatanov.frakkintoasters.event.interrupt;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.SecretEvent;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// A player's answer to an interrupt window with a character ability on its die roll, secret until the window closes,
// or no ability to pass. "Hotshot" names the Skill Card it discards, "Longshot" the result it chooses (1 to 8).
public record UseRollAbilityEvent(int playerNumber, CharacterAbility ability, SkillCard card, Integer result)
        implements PlayerEvent, SecretEvent {

    public static UseRollAbilityEvent pass(int playerNumber) {
        return new UseRollAbilityEvent(playerNumber, null, null, null);
    }

    public static UseRollAbilityEvent use(int playerNumber, CharacterAbility ability) {
        return new UseRollAbilityEvent(playerNumber, ability, null, null);
    }
}
