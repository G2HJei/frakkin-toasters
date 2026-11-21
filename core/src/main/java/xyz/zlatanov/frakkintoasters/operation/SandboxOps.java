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
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.character.Character;
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

    /**
     * Takes the card out of the deck's draw pile and puts it on top, so the next draw gives it.
     */
    public void stackOnTop(DeckId deck, Object card) {
        record(new CardsPulled(deck, List.of(card)));
        record(new CardsAddedOnTop(deck, List.of(card)));
    }

    /**
     * Puts a Skill Card on top of the Destiny deck: from the Destiny deck when it is there, otherwise from its color's
     * draw pile.
     */
    public void stackDestiny(SkillCard card) {
        val from = game.decks().destiny().cards().contains(card) ? DeckId.DESTINY : DeckId.of(card.type().color());
        record(new CardsPulled(from, List.of(card)));
        record(new CardsAddedOnTop(DeckId.DESTINY, List.of(card)));
    }

    /**
     * Gives the player a Skill Card out of its color's draw pile.
     */
    // A setup, not a loss, so no "Preventative Policy" applies.
    public void decreaseResource(Resource resource, int amount) {
        record(new ResourceDecreased(resource, amount));
    }

    public void giveSkillCard(int playerNumber, SkillCard card) {
        record(new CardsPulled(DeckId.of(card.type().color()), List.of(card)));
        record(new CardsGained(playerNumber, PlayerPile.SKILL, List.of(card)));
    }

    public void moveCharacter(Character character, Location to) {
        record(new CharacterMoved(character, to));
    }

    public void placeCylonShip(Class<? extends CylonShip> type, Location location) {
        takeFromSupply(type, location);
    }

    public void damage(Location location) {
        record(new LocationDamaged(location));
    }

    public void setJumpPreparation(JumpPreparation position) {
        record(new JumpPreparationReset());
        for (int i = 0; i < position.ordinal(); i++) {
            record(new JumpPreparationIncreased());
        }
    }
}
