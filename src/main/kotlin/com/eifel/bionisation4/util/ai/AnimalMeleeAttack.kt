package com.eifel.bionisation4.util.ai

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.MeleeAttackGoal
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.util.DamageSource
import net.minecraft.util.Hand

class AnimalMeleeAttack(animal: AnimalEntity): MeleeAttackGoal(animal, 10.0, true) {

    override fun checkAndPerformAttack(entity: LivingEntity, damage: Double) {
        val d0 = getAttackReachSqr(entity)
        if (damage <= d0 && ticksUntilNextAttack <= 0) {
            resetAttackCooldown()
            mob.swing(Hand.MAIN_HAND)
            entity.hurt(DamageSource.GENERIC, 2f)
        }
    }
}