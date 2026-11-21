package xyz.zlatanov.frakkintoasters.event.interrupt;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.interrupt.UsedRollAbility;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.interrupt.RollAbilities.canUse;

public class UseRollAbilityEventProcessor extends EventProcessor<UseRollAbilityEvent> {

    private static final int LOWEST_RESULT  = 1;
    private static final int HIGHEST_RESULT = 8;

    @Override
    protected boolean isValid() {
        val window = game.turn().interruptWindow();
        if (window.isEmpty()) {
            return false;
        }
        if (event.ability() == null) {
            return event.card() == null && event.result() == null;
        }
        if (!canUse(game, player, event.ability(), window.get())) {
            return false;
        }
        return switch (event.ability()) {
            case HOTSHOT -> event.card() != null && event.result() == null && holdsSkillCards(List.of(event.card()));
            case LONGSHOT -> event.card() == null && event.result() != null
                    && event.result() >= LOWEST_RESULT && event.result() <= HIGHEST_RESULT;
            default -> event.card() == null && event.result() == null;
        };
    }

    @Override
    public Followup process() {
        if (event.ability() != null) {
            ops.interrupts().useAbility(new UsedRollAbility(event.playerNumber(), event.ability(), event.card(),
                    event.result()));
        }
        return Followup.NONE;
    }
}
