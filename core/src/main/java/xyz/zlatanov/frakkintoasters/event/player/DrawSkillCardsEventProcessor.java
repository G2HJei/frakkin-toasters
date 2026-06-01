package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import static xyz.zlatanov.frakkintoasters.state.GameStep.RECEIVE_SKILLS;

public class DrawSkillCardsEventProcessor extends EventProcessor<DrawSkillCardsEvent> {
    @Override
    public boolean isValid() {
        return meetsDrawLimit()
                && meetsHazardousLocationReceiveSkillsStepRestrictions()
                && (player().isHuman() ? validHumanSelection() : validRevealedCylonSelection());
    }

    @Override
    public Followup process() {
        for (val entry : new TreeMap<>(event.selection()).entrySet()) {
            val deck = switch (entry.getKey()) {
                case POLITICS -> game.decks().politics();
                case LEADERSHIP -> game.decks().leadership();
                case TACTICS -> game.decks().tactics();
                case PILOTING -> game.decks().piloting();
                case ENGINEERING -> game.decks().engineering();
                case TREACHERY -> game.decks().treachery();
            };
            for (int i = 0; i < entry.getValue(); i++) {
                val card = deck.draw();
                player().skillCards().add(card);
            }
        }
        return Followup.NONE;
    }

    public boolean meetsDrawLimit() {
        val selectionCount = selectionCount();
        return switch (event.drawLimit()) {
            case null -> true;
            case DRAW_EXACTLY_2 -> selectionCount == 2;
            case DRAW_EXACTLY_3 -> selectionCount == 3;
            case DRAW_EXACTLY_5 -> selectionCount == 5;
        };
    }

    private boolean meetsHazardousLocationReceiveSkillsStepRestrictions() {
        if (game.step() != RECEIVE_SKILLS) {
            return true;
        }
        val location = game.locate(player().character());
        return switch (location) {
            case SICKBAY, RESURRECTION_SHIP -> selectionCount() == 1;
            case HUB_DESTROYED -> selectionCount() == 0;
            default -> true;
        };
    }

    private int selectionCount() {
        return event.selection().values().stream().mapToInt(Integer::intValue).sum();
    }

    private boolean validRevealedCylonSelection() {
        val selectionCount = event.selection().values()
                .stream()
                .reduce(0, Integer::sum);
        val singleSelectionPerColor = event.selection().values()
                .stream()
                .anyMatch(e -> e == 1);
        return selectionCount == 2 && singleSelectionPerColor;
    }

    private boolean validHumanSelection() {
        val skillSet = player().character().skillSet();
        val limits = new HashMap<SkillCardColor, Integer>();
        for (val option : skillSet) {
            for (val color : option.colors()) {
                limits.put(color, option.count());
            }
        }
        for (val entry : event.selection().entrySet()) {
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
        if (player().isInfiltrating()) {
            //cylon leader can select one extra card from within their skill set
            return new HashSet<>(limits.values())
                    .equals(Set.of(0, -1));
        }
        return limits.values().stream().allMatch(uses -> uses >= 0);
    }
}
