package xyz.zlatanov.frakkintoasters.state.skill;

public record SkillCard(int value, SkillCardType type) {

    public boolean hasSkillCheckAbility() {
        return type.hasSkillCheckAbility();
    }
}
