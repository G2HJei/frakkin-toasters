package xyz.zlatanov.frakkintoasters.state;

import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.DieRollView;

import java.util.ArrayList;
import java.util.List;

/**
 * One die roll, from the window before it to its result. The cards played on it survive a reroll, e.g. the +2 of a
 * "Strategic Planning" card (rule A2.1.1.10).
 */
public class DieRoll implements DieRollView {

    private static final int LOWEST  = 1;
    private static final int HIGHEST = 8;

    private       Integer             value;
    private final List<SkillCardType> cards = new ArrayList<>();
    private       int                 modifier;

    @Override
    public Integer value() {
        return value;
    }

    @Override
    public List<SkillCardType> cards() {
        return List.copyOf(cards);
    }

    @Override
    public int modifier() {
        return modifier;
    }

    @Override
    public int result() {
        if (value == null) {
            throw new FrakCallTheAdmiralException("The die wasn't rolled yet");
        }
        return Math.clamp(value + modifier, LOWEST, HIGHEST);
    }

    @Mutates
    public void roll(int value) {
        this.value = value;
    }

    @Mutates
    public void play(SkillCardType card, int modifier) {
        cards.add(card);
        this.modifier += modifier;
    }
}
