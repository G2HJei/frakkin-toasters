package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.Player;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillSetOption;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public record ReceiveSkillsAction(int player, Map<SkillCardColor, Integer> selection) implements Action {


    @Override
    public boolean isValid(Game game) {
        val player = game.player(this.player);
        return player.isRevealedCylon()
                ? validateRevealedCylonSelection()
                : validateHumanSelection(player);
    }

    @Override
    public void apply(Game game) {
        for (val entry : new TreeMap<>(selection).entrySet()) {
            val deck = switch (entry.getKey()) {
                case POLITICS -> game.decks().politics();
                case LEADERSHIP -> game.decks().leadership();
                case TACTICS -> game.decks().tactics();
                case PILOTING -> game.decks().piloting();
                case ENGINEERING -> game.decks().engineering();
                case TREACHERY -> game.decks().treachery();
            };
            for (int i = 0; i < entry.getValue(); i++) {
                game.player(player)
                        .skillCards()
                        .add(deck.draw());
            }
        }
    }

    private boolean validateRevealedCylonSelection() {
        val selectionCount = selection.values()
                .stream()
                .reduce(0, Integer::sum);
        val singleSelectionPerColor = selection.values()
                .stream()
                .anyMatch(e -> e == 1);
        return selectionCount == 2 && singleSelectionPerColor;
    }

    private boolean validateHumanSelection(Player player) {
        val skillSet = player.character().skillSet();
        val availableTypes = skillSet.stream()
                .map(SkillSetOption::availableTypes)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        return availableTypes.containsAll(selection.keySet());
    }
}
