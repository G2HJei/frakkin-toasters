package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.character.CharacterBans;
import xyz.zlatanov.frakkintoasters.event.character.CharacterPenalties;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.discardDownTo;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class RevealCylonEventProcessor extends EventProcessor<RevealCylonEvent> {

    static final int SKILL_CARDS_KEPT = 3;

    // Only a hidden Cylon reveals: a Cylon Leader has no Loyalty Cards, and a revealed Cylon has nothing left to reveal.
    public static boolean canReveal(GameView game, PlayerView player) {
        return player.character() != null
                && player.character().type() != CYLON_LEADER
                && player.isHuman()
                && player.loyaltyCards().cards().stream().anyMatch(LoyaltyCard::isCylon)
                && CharacterBans.canUseCylonCardAction(game, player); // "Spiritual Crisis"
    }

    @Override
    protected boolean isValid() {
        return canReveal(game, player)
                && event.card() != null
                && event.card().isCylon()
                && player.loyaltyCards().cards().contains(event.card());
    }

    @Override
    public Followup process() {
        val playerNumber = player.number();
        // In the "Brig" he may not use the card's power (rule 6.1).
        val inBrig = game.locate(player.character()).filter(BRIG::equals).isPresent();
        ops.player(playerNumber).revealLoyaltyCard(event.card());
        // Step 1, "Discard", before the power: "Draw Treachery Cards" draws "after you discard down to 3".
        ops.player(playerNumber).discard(player.mutinyCards().cards().toArray(MutinyCard[]::new));
        ops.player(playerNumber).loseMiracleTokens();
        ops.titles().discardQuorumCardsGivenTo(playerNumber);
        val discardSkillCards = player.skillCards().size() > SKILL_CARDS_KEPT
                ? single(new PlayerDecisionEvent<>(playerNumber, DiscardSkillCardsEvent.class, discardDownTo(SKILL_CARDS_KEPT)))
                : NONE;
        return allOf(
                CharacterPenalties.cylonCardRevealed(game), // "Lies and Secrets"
                discardSkillCards,
                inBrig ? NONE : CylonRevealPowers.power(game, playerNumber, event.card()),
                single(new FinishCylonRevealEvent(playerNumber, true)));
    }
}
