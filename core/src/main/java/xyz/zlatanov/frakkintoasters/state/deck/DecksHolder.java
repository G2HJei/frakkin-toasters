package xyz.zlatanov.frakkintoasters.state.deck;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.view.DecksHolderView;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.*;
import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.*;

@Builder
@Getter
@Accessors(fluent = true)
public class DecksHolder implements DecksHolderView {
    @Builder.Default
    private       Deck<CivilianShip>    civilianShips   = civilianShipsDeck();
    @Builder.Default
    private final Deck<GalacticaDamage> galacticaDamage = genericDeck(GalacticaDamage.class, false);
    @Builder.Default
    private final Deck<PegasusDamage>   pegasusDamage   = genericDeck(PegasusDamage.class, false);
    @Builder.Default
    private final Deck<BasestarDamage>  basestarDamage  = genericDeck(BasestarDamage.class, false);
    @Builder.Default
    private final Deck<DestinationCard> destination     = genericDeck(DestinationCard.class, REMOTE_PLANET, ICY_MOON, BARREN_PLANET, TYLIUM_PLANET, TYLIUM_PLANET, TYLIUM_PLANET);
    @Builder.Default
    private final Deck<SkillCard>       politics        = politicsCards();
    @Builder.Default
    private final Deck<SkillCard>       leadership      = leadershipCards();
    @Builder.Default
    private final Deck<SkillCard>       tactics         = tacticsCards();
    @Builder.Default
    private final Deck<SkillCard>       piloting        = pilotingCards();
    @Builder.Default
    private final Deck<SkillCard>       engineering     = engineeringCards();
    @Builder.Default
    private final Deck<SkillCard>       treachery       = treacheryCards();
    @Builder.Default
    private final Deck<SkillCard>       destiny         = new Deck<>(false);
    @Builder.Default
    private final Deck<QuorumCard>      quorum          = genericDeck(QuorumCard.class);
    @Builder.Default
    private final Deck<CrisisCard>      crisis          = genericDeck(CrisisCard.class);
    @Builder.Default
    private final Deck<SuperCrisisCard> superCrisis     = genericDeck(SuperCrisisCard.class);
    @Builder.Default
    private final Deck<LoyaltyCard>     loyalty         = new Deck<>(false);
    @Builder.Default
    private final Deck<LoyaltyCard>     loyaltyNotCylon = new Deck<>(false);
    @Builder.Default
    private final Deck<MutinyCard>      mutiny          = genericDeck(MutinyCard.class);
    @Builder.Default
    private final Deck<MotiveCard>      motive          = genericDeck(MotiveCard.class);

    public void discard(SkillCard... cards) {
        discard(List.of(cards));
    }

    public void discard(Enum<?>... cards) {
        discard(List.of(cards));
    }

    public void discard(List<?> cards) {
        cards.forEach(this::discard);
    }

    public void discard(Object card) {
        switch (card) {
            case SkillCard skillCard -> {
                val deck = switch (skillCard.type().color()) {
                    case POLITICS -> politics;
                    case LEADERSHIP -> leadership;
                    case TACTICS -> tactics;
                    case PILOTING -> piloting;
                    case ENGINEERING -> engineering;
                    case TREACHERY -> treachery;
                };
                deck.discard(skillCard);
            }
            // damage cards go back on top; the calling op shuffles the deck (it has to be logged)
            case GalacticaDamage dmg -> galacticaDamage.addOnTop(dmg);
            case BasestarDamage dmg -> basestarDamage.addOnTop(dmg);
            case PegasusDamage dmg -> pegasusDamage.addOnTop(dmg);
            case MutinyCard mutinyCard -> {
                mutiny.discard(mutinyCard);
            }
            case null, default ->
                //todo
                    throw new FrakCallTheAdmiralException();
        }
    }

    /**
     * @return the deck with this id. The caller picks the card type
     */
    @SuppressWarnings("unchecked")
    @Override
    public int skillCardsLeft(SkillCardColor color) {
        Deck<SkillCard> deck = deck(DeckId.of(color));
        return deck.size() + deck.discardSize();
    }

    public <T> Deck<T> deck(DeckId id) {
        return (Deck<T>) switch (id) {
            case CIVILIAN_SHIPS -> civilianShips;
            case GALACTICA_DAMAGE -> galacticaDamage;
            case PEGASUS_DAMAGE -> pegasusDamage;
            case BASESTAR_DAMAGE -> basestarDamage;
            case DESTINATION -> destination;
            case POLITICS -> politics;
            case LEADERSHIP -> leadership;
            case TACTICS -> tactics;
            case PILOTING -> piloting;
            case ENGINEERING -> engineering;
            case TREACHERY -> treachery;
            case DESTINY -> destiny;
            case QUORUM -> quorum;
            case CRISIS -> crisis;
            case SUPER_CRISIS -> superCrisis;
            case LOYALTY -> loyalty;
            case LOYALTY_NOT_CYLON -> loyaltyNotCylon;
            case MUTINY -> mutiny;
            case MOTIVE -> motive;
        };
    }
}
