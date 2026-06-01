package xyz.zlatanov.frakkintoasters.state.deck;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
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

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.*;
import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.*;

@Builder
@Getter
@Accessors(fluent = true)
public class DecksHolder {
    @Builder.Default
    private       Deck<CivilianShip>    civilianShips   = civilianShipsDeck();
    @Builder.Default
    private final Deck<GalacticaDamage> galacticaDamage = genericDeck(GalacticaDamage.class);
    @Builder.Default
    private final Deck<PegasusDamage>   pegasusDamage   = genericDeck(PegasusDamage.class);
    @Builder.Default
    private final Deck<BasestarDamage>  basestarDamage  = genericDeck(BasestarDamage.class);
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
    private final Deck<SkillCard>       destiny         = new Deck<>();
    @Builder.Default
    private final Deck<QuorumCard>      quorum          = genericDeck(QuorumCard.class);
    @Builder.Default
    private final Deck<CrisisCard>      crisis          = genericDeck(CrisisCard.class);
    @Builder.Default
    private final Deck<SuperCrisisCard> superCrisis     = genericDeck(SuperCrisisCard.class);
    @Builder.Default
    private final Deck<LoyaltyCard>     loyalty         = new Deck<>();
    @Builder.Default
    private final Deck<LoyaltyCard>     loyaltyNotCylon = new Deck<>();
    @Builder.Default
    private final Deck<MutinyCard>      mutiny          = genericDeck(MutinyCard.class);

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
            case GalacticaDamage dmg -> {
                galacticaDamage.addOnTop(dmg);
                galacticaDamage.shuffle();
            }
            case BasestarDamage dmg -> {
                basestarDamage.addOnTop(dmg);
                basestarDamage.shuffle();
            }
            case PegasusDamage dmg -> {
                pegasusDamage.addOnTop(dmg);
                pegasusDamage.shuffle();
            }
            case null, default ->
                //todo
                    throw new FrakCallTheAdmiralException();
        }
    }
}
