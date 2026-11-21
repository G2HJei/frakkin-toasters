package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.Objects;

public class ChooseGalacticaDamageTokensEventProcessor extends EventProcessor<ChooseGalacticaDamageTokensEvent> {

    @Override
    protected boolean isValid() {
        return event.resolved() != null && event.returned() != null
                && event.resolved().stream().allMatch(Objects::nonNull)
                && event.returned().stream().allMatch(Objects::nonNull);
    }

    @Override
    public Followup process() {
        event.resolved().forEach(token -> ops.galactica().resolveDamage(token));
        event.returned().forEach(token -> ops.galactica().returnDamage(token));
        return Followup.NONE;
    }
}
