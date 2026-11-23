package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.ALERT_VIPER_PILOT;

// Lee "Apollo" Adama's "Alert Viper Pilot": when a viper goes from the "Reserves" into a space area, he may pilot it
// and take 1 action, while he is on a Galactica location other than the "Brig".
@Source(ability = ALERT_VIPER_PILOT)
public record AlertViperPilotEvent(int playerNumber, int viperId) implements PlayerEvent {
}
