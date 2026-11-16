package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The Mutineer reveals as a Cylon or is executed as one, and passes the "You Are the Mutineer" card to a human player of
// his choice, who draws a Mutiny Card and gives away his titles (rule 6.1.1).
//
// @param recipientDrawsLoyaltyCard a Cylon who was dealt the card in the Sleeper Agent phase passes it on, and the
//                                  recipient reveals it as if it had been dealt to him, so he draws another Loyalty Card
//                                  (rule 5.5.2.2). Not so when the Mutineer reveals as a Cylon (rule 5.2.2)
public record PassMutineerCardEvent(int playerNumber, int recipientPlayerNumber, boolean recipientDrawsLoyaltyCard)
        implements PlayerEvent {

    public PassMutineerCardEvent(int playerNumber, int recipientPlayerNumber) {
        this(playerNumber, recipientPlayerNumber, false);
    }
}
