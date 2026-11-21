package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.player.TreacheryDiscards;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.NOT_CYLON_SELFISH;

public class RevealPersonalGoalEventProcessor extends EventProcessor<RevealPersonalGoalEvent> {

    static final int SELFISH_STRENGTH   = 20;
    static final int SACRIFICED_VIPERS  = 6;
    static final int RAIDERS_TO_FIGHT   = 10;
    static final int TITLES_FOR_POWER   = 2;
    static final int REDRAW_DISTANCE    = 6;

    /**
     * @return whether the player can reveal one of his Personal Goals now. A hidden Cylon may too: until he reveals, he
     * is a human player. A revealed Cylon takes no action but his location's (rule 6.1.3.3), and a Cylon Leader has no
     * Loyalty Cards
     */
    public static boolean canReveal(GameView game, PlayerView player) {
        return player.character() != null
                && player.isHuman()
                && player.loyaltyCards().cards().stream().distinct().anyMatch(card -> canReveal(game, player, card));
    }

    /**
     * @return whether the condition printed on the card holds now. For "Selfish", whether his hand is worth 20 strength
     */
    public static boolean canReveal(GameView game, PlayerView player, LoyaltyCard card) {
        val galactica = game.boards().galactica();
        return switch (card) {
            case NOT_CYLON_ACQUIRE_POWER -> titles(game, player) >= TITLES_FOR_POWER;
            case NOT_CYLON_DEVASTATION -> game.nukes() == 0;
            case NOT_CYLON_POLITICAL_INTRIGUE -> game.locate(game.president()).filter(BRIG::equals).isPresent();
            case NOT_CYLON_SACRIFICE -> damagedOrDestroyedVipers(game) >= SACRIFICED_VIPERS;
            case NOT_CYLON_SELFISH -> strength(player.skillCards().cards()) >= SELFISH_STRENGTH;
            case NOT_CYLON_SELF_DESTRUCTION -> game.locate(player.character())
                    .filter(location -> location == BRIG || location == SICKBAY)
                    .isPresent();
            case NOT_CYLON_STAND_AND_FIGHT -> galactica.shipsInSpace(Raider.class).size() >= RAIDERS_TO_FIGHT;
            case NOT_CYLON_USE_CAUTION -> game.destinations().stream().map(DestinationCard::distance).anyMatch(d -> d == 1);
            case NOT_CYLON, FINAL_FIVE_YOU_ARE_SENT_TO_BRIG, FINAL_FIVE_YOU_ARE_EXECUTED, FINAL_FIVE_THEY_ARE_EXECUTED,
                 FINAL_FIVE_DAMAGE_GALACTICA_TWICE, FINAL_FIVE_CYLON_SHIPS_ACTIVATE, MUTINEER, CYLON_CAN_PLACE_CENTURION,
                 CYLON_DECREASE_JUMP_TRACK, CYLON_DRAW_TREACHERY_CARDS, CYLON_SEND_TO_BRIG, CYLON_DAMAGE_GALACTICA,
                 CYLON_REDUCE_MORALE, CYLON_SEND_TO_SICKBAY -> false;
        };
    }

    @Override
    protected boolean isValid() {
        return player.character() != null
                && player.isHuman()
                && event.card() != null
                && player.loyaltyCards().cards().contains(event.card())
                && (event.card() == NOT_CYLON_SELFISH
                ? isSelfishDiscard()
                : discards().isEmpty() && canReveal(game, player, event.card()));
    }

    @Override
    public Followup process() {
        val playerOps = ops.player(player.number());
        // "Selfish": he discards the cards to reveal it.
        playerOps.discard(discards().toArray(SkillCard[]::new));
        playerOps.revealLoyaltyCard(event.card());
        val mutinyCard = TreacheryDiscards.mutinyCardFor(game, ops, player.number(), discards());
        if (game.distanceTraveled() > REDRAW_DISTANCE) {
            return mutinyCard;
        }
        // Up to 6 distance a "You Are Not a Cylon" card goes into the Loyalty deck and he draws a new Loyalty Card (rule
        // 5.3.1.1), as after an execution.
        ops.loyaltyDeck().addNotACylon();
        return allOf(single(new DealLoyaltyCardsEvent(player.number(), 1, false)), mutinyCard);
    }

    // The cards are in his hand and worth 20 strength together.
    private boolean isSelfishDiscard() {
        return !discards().isEmpty() && holdsSkillCards(discards()) && strength(discards()) >= SELFISH_STRENGTH;
    }

    private List<SkillCard> discards() {
        // Clients may send no list at all.
        return event.discards() == null ? List.of() : event.discards().stream().filter(Objects::nonNull).toList();
    }

    private static int strength(List<SkillCard> cards) {
        return cards.stream().mapToInt(SkillCard::value).sum();
    }

    private static long titles(GameView game, PlayerView player) {
        return Stream.of(game.president(), game.admiral(), game.cag()).filter(player.character()::equals).count();
    }

    // The vipers in the damaged vipers box, Mark VIIs included, and the vipers and assault raptors destroyed, which count
    // as vipers (rules 11.3.1.1, 11.3.2.2).
    private static long damagedOrDestroyedVipers(GameView game) {
        val damaged = game.boards().galactica().damagedShips().size();
        val destroyed = game.removedComponents().items().stream()
                .filter(item -> item instanceof Viper || item instanceof AssaultRaptor)
                .count();
        return damaged + destroyed;
    }
}
