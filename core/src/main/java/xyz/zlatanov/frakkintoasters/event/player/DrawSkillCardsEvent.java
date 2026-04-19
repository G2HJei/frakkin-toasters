package xyz.zlatanov.frakkintoasters.event.player;

import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.*;

import static xyz.zlatanov.frakkintoasters.state.GameStep.RECEIVE_SKILLS;

@Accessors(fluent = true)
public record DrawSkillCardsEvent(int playerNumber,
                                  Map<SkillCardColor, Integer> selection,
                                  List<EventConstraint> eventConstraints) implements PlayerEvent {

    public DrawSkillCardsEvent(int playerNumber, Map<SkillCardColor, Integer> selection,
                               EventConstraint... eventConstraints) {
        this(playerNumber, selection, List.of(eventConstraints));
    }

    @Override
    public boolean isValidConstraint(Game game, EventConstraint constraint) {
        return switch (constraint) {
            case DRAW_EXACTLY_2 -> totalCardsToReceive() == 2;
            case DRAW_EXACTLY_3 -> totalCardsToReceive() == 3;
        };
    }

    @Override
    public boolean isValid(Game game) {
        if (!meetsHazardousLocationRestrictions(game)) {
            return false;
        }
        val player = player(game);
        return player.isHuman()
                ? validateHumanSelection(player)
                : validateRevealedCylonSelection();
    }

    @Override
    public Followup apply(Game game) {
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
        return Followup.NONE;
    }

    private boolean meetsHazardousLocationRestrictions(Game game) {
        if (game.step() != RECEIVE_SKILLS) {
            return true;
        }
        val location = game.locate(player(game).character());
        val cardsToReceive = totalCardsToReceive();
        return switch (location) {
            case SICKBAY, RESURRECTION_SHIP -> cardsToReceive == 1;
            case HUB_DESTROYED -> cardsToReceive == 0;
            default -> true;
        };
    }

    private int totalCardsToReceive() {
        return selection.values().stream().mapToInt(Integer::intValue).sum();
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
