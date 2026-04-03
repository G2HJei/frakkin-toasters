package xyz.zlatanov.frakkintoasters.action.skills;

import lombok.val;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.action.Action;
import xyz.zlatanov.frakkintoasters.state.skill.SkillSetOption;

import java.util.Collection;
import java.util.List;

public record ReceiveSkillsAction(int player, List<SkillSelection> selection) implements Action {


    @Override
    public boolean isValid(Game game) {
        val skillSet = game.player(player).character().skillSet();
        val availableTypes = skillSet.stream()
                .map(SkillSetOption::availableTypes)
                .flatMap(Collection::stream)
                .toList();
        return selection.stream()
                .allMatch(selection -> availableTypes.contains(selection.color()));
    }

    @Override
    public void apply(Game game) {
        game.player(player).skillCards().add(game.decks().leadership().draw());
    }
}
