package xyz.zlatanov.frakkintoasters.event.player;

import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.*;

@Accessors(fluent = true)
public record ReceiveSkillCardsEvent(int playerNumber,
                                     Map<SkillCardColor, Integer> selection,
                                     EventConstraint eventConstraint) implements PlayerEvent {

    public ReceiveSkillCardsEvent(int playerNumber, Map<SkillCardColor, Integer> selection) {
        this(playerNumber, selection, null);
    }

    @Override
    public boolean isValid(Game game) {
        if (eventConstraint != null) {
            val total = selection.values().stream().mapToInt(Integer::intValue).sum();
            switch (eventConstraint) {
                case DRAW_EXACTLY_2 -> {
                    if (total != 2) {
                        return false;
                    }
                }
                default -> throw new FrakCallTheAdmiralException(
                        "Unsupported constraint for ReceiveSkillCardsEvent: " + eventConstraint);
            }
        }
        val player = player(game);
        return player.isHuman()
                ? validateHumanSelection(player)
                : validateRevealedCylonSelection();
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
        if (player.isInfiltrating()) {
            //cylon leader can select one extra card from within their skill set
            return new HashSet<>(limits.values())
                    .equals(Set.of(0, -1));
        }
        return limits.values().stream().allMatch(uses -> uses >= 0);
    }
}
