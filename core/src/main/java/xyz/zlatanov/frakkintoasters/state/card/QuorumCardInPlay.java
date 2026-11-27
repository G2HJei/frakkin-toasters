package xyz.zlatanov.frakkintoasters.state.card;

/**
 * @param holderPlayerNumber the player it was given to, e.g. the Arbitrator; {@code null} for "Accept Prophecy"
 */
public record QuorumCardInPlay(QuorumCard card, Integer holderPlayerNumber) {
}
