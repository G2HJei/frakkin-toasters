package xyz.zlatanov.frakkintoasters.event.player;

import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Accessors(fluent = true)
public record ReceiveSkillsEvent(int playerNumber, Map<SkillCardColor, Integer> selection) implements PlayerEvent {
    @Override
    public boolean isValid(Game game) {
        val player = player(game);
        return player.isRevealedCylon()
                ? validateRevealedCylonSelection()
                : validateHumanSelection(player);
    }

    @Override
    public List<Followup> apply(Game game) {
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
                player(game)
                        .skillCards()
                        .add(deck.draw());
            }
        }
        return List.of();
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
        val limits = new HashMap<SkillCardColor, Integer>();
        for (val option : skillSet) {
            for (val color : option.colors()) {
                limits.put(color, option.count());
            }
        }
        for (val entry : selection.entrySet()) {
            val color = entry.getKey();
            val count = entry.getValue();

            for (int i = 0; i < count; i++) {
                val canUse = skillSet.stream()
                        .anyMatch(option -> option.colors().contains(color));

                if (!canUse) {
                    return false;
                }
                skillSet.stream()
                        .filter(option -> option.colors().contains(color))
                        .forEach(option -> {
                            for (val c : option.colors()) {
                                limits.merge(c, -1, Integer::sum);
                            }
                        });
            }
        }
        return limits.values().stream().allMatch(uses -> uses >= 0);
    }
}
