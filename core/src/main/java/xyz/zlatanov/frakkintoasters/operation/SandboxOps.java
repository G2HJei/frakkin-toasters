package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.JumpPreparationIncreased;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.JumpPreparationReset;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.LocationDamaged;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.ResourceDecreased;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsPulled;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.CardsGained;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.CharacterMoved;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.TitleFact.PresidentQuorumCardGained;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.track.JumpPreparation;

import java.util.List;

/**
 * What a sandbox setup does that no rule does: stacking a chosen card, giving one, moving a character or damaging a
 * location of the setup's choice. The setup events use the ops of the rules where one fits.
 */
public class SandboxOps extends AbstractOps {

    public SandboxOps(Game game, FactRecorder recorder) {
        super(game, recorder);
    }

    public void stackOnTop(DeckId deck, Object card) {
        record(new CardsPulled(deck, List.of(card)));
        record(new CardsAddedOnTop(deck, List.of(card)));
    }

    public void stackDestiny(SkillCard card) {
        val from = game.decks().destiny().cards().contains(card) ? DeckId.DESTINY : DeckId.of(card.type().color());
        record(new CardsPulled(from, List.of(card)));
        record(new CardsAddedOnTop(DeckId.DESTINY, List.of(card)));
    }

    // A setup, not a loss, so no "Preventative Policy" applies.
    public void decreaseResource(Resource resource, int amount) {
        record(new ResourceDecreased(resource, amount));
    }

    public void giveSkillCard(int playerNumber, SkillCard card) {
        record(new CardsPulled(DeckId.of(card.type().color()), List.of(card)));
        record(new CardsGained(playerNumber, PlayerPile.SKILL, List.of(card)));
    }

    public void giveMutinyCard(int playerNumber, MutinyCard card) {
        record(new CardsPulled(DeckId.MUTINY, List.of(card)));
        record(new CardsGained(playerNumber, PlayerPile.MUTINY, List.of(card)));
    }

    /**
     * @param from {@code null} for a "You Are a Cylon" card, which isn't in a deck before it is dealt
     */
    public void giveLoyaltyCard(int playerNumber, LoyaltyCard card, DeckId from) {
        if (from != null) {
            record(new CardsPulled(from, List.of(card)));
        }
        record(new CardsGained(playerNumber, PlayerPile.LOYALTY, List.of(card)));
    }

    public void giveSuperCrisisCard(int playerNumber, SuperCrisisCard card) {
        record(new CardsPulled(DeckId.SUPER_CRISIS, List.of(card)));
        record(new CardsGained(playerNumber, PlayerPile.SUPER_CRISIS, List.of(card)));
    }

    public void giveQuorumCardToPresident(QuorumCard card) {
        record(new CardsPulled(DeckId.QUORUM, List.of(card)));
        record(new PresidentQuorumCardGained(card));
    }

    public void moveCharacter(Character character, Location to) {
        record(new CharacterMoved(character, to));
    }

    public void placeCylonShip(Class<? extends CylonShip> type, Location location) {
        takeFromSupply(type, location);
    }

    // The location's damage token is in play, out of its pile, as if it had been drawn, so a repair returns it once.
    public void damage(Location location) {
        DamageTokens.of(location)
                .filter(token -> game.decks().deck(DamageTokens.deckOf(token)).cards().contains(token))
                .ifPresent(token -> record(new CardsPulled(DamageTokens.deckOf(token), List.of(token))));
        record(new LocationDamaged(location));
    }

    public void setJumpPreparation(JumpPreparation position) {
        record(new JumpPreparationReset());
        for (int i = 0; i < position.ordinal(); i++) {
            record(new JumpPreparationIncreased());
        }
    }
}
