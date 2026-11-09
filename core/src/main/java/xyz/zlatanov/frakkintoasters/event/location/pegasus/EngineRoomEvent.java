package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.state.board.Location.ENGINE_ROOM;

// Engine Room (Pegasus location)
// Action: Discard 2 Skill Cards to treat the next Crisis Card drawn this turn as if it had a "prepare for jump" icon.
public record EngineRoomEvent(int playerNumber, SkillCard discardCard1, SkillCard discardCard2) implements ActionEvent {

    @Override
    public Location location() {
        return ENGINE_ROOM;
    }
}
