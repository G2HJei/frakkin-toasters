package xyz.zlatanov.frakkintoasters.state.deck;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.*;
import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.*;

@Builder
@Getter
@Accessors(fluent = true)
public class DecksHolder {
    @Builder.Default
    private       Deck<CivilianShip>    civilianShips   = setupCivilianShipsDeck();
    @Builder.Default
    private final Deck<GalacticaDamage> galacticaDamage = setupGenericDeck(GalacticaDamage.class);
    @Builder.Default
    private final Deck<PegasusDamage>   pegasusDamage   = setupGenericDeck(PegasusDamage.class);
    @Builder.Default
    private final Deck<BasestarDamage>  basestarDamage  = setupGenericDeck(BasestarDamage.class);
    @Builder.Default
    private final Deck<DestinationCard> destination     = setupGenericDeck(DestinationCard.class, REMOTE_PLANET, ICY_MOON, BARREN_PLANET, TYLIUM_PLANET, TYLIUM_PLANET, TYLIUM_PLANET);
    @Builder.Default
    private final Deck<SkillCard>       politics        = allPoliticsCards();
    @Builder.Default
    private final Deck<SkillCard>       leadership      = allLeadershipCards();
    @Builder.Default
    private final Deck<SkillCard>       tactics         = allTacticsCards();
    @Builder.Default
    private final Deck<SkillCard>       piloting        = allPilotingCards();
    @Builder.Default
    private final Deck<SkillCard>       engineering     = allEngineeringCards();
    @Builder.Default
    private final Deck<SkillCard>       treachery       = allTreacheryCards();
    @Builder.Default
    private final Deck<QuorumCard>      quorum          = setupGenericDeck(QuorumCard.class);
    @Builder.Default
    private final Deck<CrisisCard>      crisis          = setupGenericDeck(CrisisCard.class);
    @Builder.Default
    private final Deck<SuperCrisisCard> superCrisis     = setupGenericDeck(SuperCrisisCard.class);
    @Builder.Default
    private final Deck<LoyaltyCard>     loyalty         = new Deck<>();
    @Builder.Default
    private final Deck<LoyaltyCard>     loyaltyNotCylon = new Deck<>();
    @Builder.Default
    private final Deck<MutinyCard>      mutiny          = setupGenericDeck(MutinyCard.class);

}
