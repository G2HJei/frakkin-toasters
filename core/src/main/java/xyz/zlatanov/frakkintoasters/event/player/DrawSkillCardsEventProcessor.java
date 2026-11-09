package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.character.SkillSetOption;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.*;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.state.GameStep.RECEIVE_SKILLS;
import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class DrawSkillCardsEventProcessor extends EventProcessor<DrawSkillCardsEvent> {

    @Override
    public boolean isValid() {
        val colors = colorsToDraw();
        return colors != null && canDraw(colors) && meetsDrawLimit(colors);
    }

    @Override
    public Followup process() {
        ops.player(player.number()).drawSkillCards(colorsToDraw());
        return Followup.NONE;
    }

    private boolean canDraw(List<SkillCardColor> colors) {
        return leftInTheDecks(colors)
                && meetsHazardousLocationReceiveSkillsStepRestrictions(colors)
                && (drawsFromSkillSet() ? fitsSkillSet(colors) : fitsRevealedCylon(colors));
    }

    // The player draws fewer only when no other card he could draw is left in the decks (rule 8.2.2.5). The cards he
    // can draw together form a matroid, so a selection that no card can be added to is as large as any.
    private boolean meetsDrawLimit(List<SkillCardColor> colors) {
        if (event.drawLimit() == null) {
            return true;
        }
        val limit = count(event.drawLimit());
        return colors.size() == limit
                || colors.size() < limit && Arrays.stream(SkillCardColor.values()).noneMatch(c -> canDraw(plus(colors, c)));
    }

    private boolean leftInTheDecks(List<SkillCardColor> colors) {
        return colors.stream()
                .distinct()
                .allMatch(c -> Collections.frequency(colors, c) <= game.decks().skillCardsLeft(c));
    }

    private boolean meetsHazardousLocationReceiveSkillsStepRestrictions(List<SkillCardColor> colors) {
        if (game.turn().step() != RECEIVE_SKILLS) {
            return true;
        }
        val location = game.locate(player.character()).orElse(null);
        return switch (location) {
            case SICKBAY, RESURRECTION_SHIP -> colors.size() <= 1;
            case null, default -> true;
        };
    }

    // Humans and Cylon Leaders draw from their skill set (rule 6.2.1.1), revealed Cylons from any deck.
    private boolean drawsFromSkillSet() {
        return player.isHuman() || player.character().type() == CYLON_LEADER;
    }

    // Each card takes the place of an option that has its color. Options can share a color, as in Helena Cain's
    // "2xL, 2xTa, 1xTa/L", so a card may have to try each of them.
    private boolean fitsSkillSet(List<SkillCardColor> colors) {
        val options = List.copyOf(player.character().skillSet());
        val room = options.stream().mapToInt(SkillSetOption::count).toArray();
        return fits(colors, 0, options, room, extraCards());
    }

    private static boolean fits(List<SkillCardColor> colors, int next, List<SkillSetOption> options, int[] room,
                                int extra) {
        if (next == colors.size()) {
            return true;
        }
        val color = colors.get(next);
        for (int i = 0; i < options.size(); i++) {
            if (room[i] > 0 && options.get(i).colors().contains(color)) {
                room[i]--;
                val fits = fits(colors, next + 1, options, room, extra);
                room[i]++;
                if (fits) {
                    return true;
                }
            }
        }
        // The extra card can be of any color of the skill set, also one already drawn (rule 8.2.2.4).
        val inSkillSet = options.stream().anyMatch(option -> option.colors().contains(color));
        return extra > 0 && inSkillSet && fits(colors, next + 1, options, room, extra - 1);
    }

    // An Infiltrating Cylon Leader draws 1 more in his Receive Skills step (rule 8.2.2.4), and at the start of the
    // game, as Sharon "Athena" Agathon does (rule 6.2.1.1).
    private int extraCards() {
        val step = game.turn().step();
        return player.isInfiltrating() && (step == RECEIVE_SKILLS || step == SETUP) ? 1 : 0;
    }

    // A revealed Cylon draws each card from a different deck, 2 at most (rule 6.1.3.1).
    private static boolean fitsRevealedCylon(List<SkillCardColor> colors) {
        return colors.size() <= 2 && new HashSet<>(colors).size() == colors.size();
    }

    // The cards to draw, in the order of the colors, or null when the event names no proper selection.
    private List<SkillCardColor> colorsToDraw() {
        val selection = event.selection();
        if (selection == null || selection.entrySet().stream().anyMatch(e -> e.getKey() == null || e.getValue() == null || e.getValue() < 0)) {
            return null;
        }
        return new TreeMap<>(selection).entrySet()
                .stream()
                .flatMap(e -> Stream.generate(e::getKey).limit(e.getValue()))
                .toList();
    }

    private static List<SkillCardColor> plus(List<SkillCardColor> colors, SkillCardColor color) {
        val more = new ArrayList<>(colors);
        more.add(color);
        return more;
    }

    private static int count(EventConstraint drawLimit) {
        return switch (drawLimit) {
            case DRAW_EXACTLY_1 -> 1;
            case DRAW_EXACTLY_2 -> 2;
            case DRAW_EXACTLY_3 -> 3;
            case DRAW_EXACTLY_4 -> 4;
            case DRAW_EXACTLY_5 -> 5;
            default -> throw new FrakCallTheAdmiralException("A draw doesn't take " + drawLimit);
        };
    }
}
