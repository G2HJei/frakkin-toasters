package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

// Launch Reserves - color: PILOTING, value: 5
// Action: place up to 2 unmanned vipers from the "Reserves" in a space area that contains a piloted viper, then
// activate them.
//
// @param area      the space area with a piloted viper
// @param shipTypes 1 or 2 fighters from the "Reserves": vipers, Mark VIIs or assault raptors, which count as vipers
public record LaunchReservesEvent(int playerNumber, SkillCard card, Location area, List<ShipType> shipTypes)
        implements SkillCardEvent, ActionEvent {
}
