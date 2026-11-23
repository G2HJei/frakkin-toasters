package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.NECESSARY_STEPS;

// Tom Zarek (alt.)'s "Necessary Steps": when any player draws a Mutiny Card, Zarek looks at the top 2, gives that player
// one (given) and puts the other at the bottom (bottom, null when only 1 was left). staysPut: the player doesn't go to
// the "Brig" for it (the "Press Room").
@Source(ability = NECESSARY_STEPS)
public record NecessaryStepsEvent(int playerNumber, int drawerPlayerNumber, MutinyCard given, MutinyCard bottom,
                                  boolean staysPut) implements PlayerEvent {
}
