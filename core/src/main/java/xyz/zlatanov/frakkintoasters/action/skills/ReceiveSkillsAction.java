package xyz.zlatanov.frakkintoasters.action.skills;

import lombok.val;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.Player;
import xyz.zlatanov.frakkintoasters.action.Action;
import xyz.zlatanov.frakkintoasters.state.skill.SkillSetOption;

import java.util.Collection;
import java.util.List;

public record ReceiveSkillsAction(int player, List<SkillSelection> selection) implements Action {


    @Override
    public boolean isValid(Game game) {
        val player = game.player(this.player);
        val skillSet = player.character().skillSet();
        val availableTypes = skillSet.stream()
                .map(SkillSetOption::availableTypes)
                .flatMap(Collection::stream)
                .toList();
        return player.isRevealedCylon()
                ? validateRevealedCylonSelection(player)
                : selection.stream()
                  .allMatch(selection -> availableTypes.contains(selection.color()));
    }

    @Override
    public void apply(Game game) {
        for (val select : selection) {
            val deck = switch (select.color()) {
                case POLITICS -> game.decks().politics();
                case LEADERSHIP -> game.decks().leadership();
                case TACTICS -> game.decks().tactics();
                case PILOTING -> game.decks().piloting();
                case ENGINEERING -> game.decks().engineering();
                case TREACHERY -> game.decks().treachery();
            };
            for (int i = 0; i < select.count(); i++) {
                game.player(player)
                        .skillCards()
                        .add(deck.draw());
            }
        }
    }

    private boolean validateRevealedCylonSelection(Player player) {
        val totalCards = selection.stream()
                .map(SkillSelection::count)
                .reduce(0, Integer::sum);
        return totalCards == 2;
    }
}
