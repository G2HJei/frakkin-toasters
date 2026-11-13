package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;

import java.util.Arrays;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;

public class BasestarBridgeEventProcessor extends LocationActionProcessor<BasestarBridgeEvent> {

    @Override
    protected boolean isValidAction() {
        return event.firstAbility() != null;
    }

    // "he chooses two of the four listed abilities to resolve, one at a time. Note that he may only resolve each ability
    // once per activation of the location." (rule 9.7)
    @Override
    public Followup process() {
        val playerNumber = player.number();
        val first = event.firstAbility();
        val second = one(Arrays.stream(BasestarBridgeAbility.values())
                .filter(ability -> ability != first)
                .map(ability -> (Event) new BasestarBridgeAbilityEvent(playerNumber, ability))
                .toArray(Event[]::new));
        return all(single(new BasestarBridgeAbilityEvent(playerNumber, first)), second);
    }
}
