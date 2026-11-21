package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Send a Message (Mutiny Card)
// Action: damage Galactica, then, if there is one, attack a centurion on the Boarding Party track with 2 added to the
// roll. Discard it.
//
// @param centurionId the centurion, null when the track is empty
public record SendAMessageEvent(int playerNumber, Integer centurionId) implements MutinyCardEvent {
}
