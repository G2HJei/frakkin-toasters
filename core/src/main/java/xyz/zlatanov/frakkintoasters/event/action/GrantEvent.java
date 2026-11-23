package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.Title;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

/**
 * Lets the player act now, e.g. the target of an "Executive Order". It is queued, never submitted. When it runs, the
 * player becomes the acting player, and his choice is offered as the game stands then, so grants can nest.
 *
 * @param grant     what he may do
 * @param grantedBy what lets him, whose rules apply
 */
public record GrantEvent(int playerNumber, Grant grant, GrantedBy grantedBy) implements PlayerEvent {

    public enum Grant {
        ACTION,
        MOVE,
        MOVE_OR_ACTION
    }

    // Each names its card, location, title or ability, so the table says why the player may act. The constants share
    // their names, hence the qualified ones.
    public enum GrantedBy {
        @Source(location = Location.HANGAR_DECK)
        HANGAR_DECK,
        @Source(skillCard = SkillCardType.EXECUTIVE_ORDER)
        EXECUTIVE_ORDER,
        @Source(skillCard = SkillCardType.STATE_OF_EMERGENCY)
        STATE_OF_EMERGENCY,
        @Source(title = Title.CAG)
        CAG_TITLE,
        @Source(ability = CharacterAbility.EXPERT_PILOT)
        EXPERT_PILOT,
        @Source(ability = CharacterAbility.METICULOUS)
        METICULOUS,
        @Source(ability = CharacterAbility.AGGRESSIVE_TACTICS)
        AGGRESSIVE_TACTICS,
        @Source(ability = CharacterAbility.MAINTENANCE_ENGINEER)
        MAINTENANCE_ENGINEER,
        @Source(ability = CharacterAbility.ALERT_VIPER_PILOT)
        ALERT_VIPER_PILOT
    }
}
