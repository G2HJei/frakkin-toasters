package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

public record DetermineSkillCheckAbilitiesOrderEvent(int playerNumber, List<SkillCard> cardsWithAbilities)
        implements PlayerEvent {

}
