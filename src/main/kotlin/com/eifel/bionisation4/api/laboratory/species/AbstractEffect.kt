package com.eifel.bionisation4.api.laboratory.species

import com.eifel.bionisation4.api.constant.InternalConstants
import com.eifel.bionisation4.api.laboratory.util.EffectType
import com.eifel.bionisation4.api.laboratory.util.IGene
import com.eifel.bionisation4.api.laboratory.util.INBTSerializable
import com.eifel.bionisation4.common.config.ConfigProperties
import com.eifel.bionisation4.common.extensions.doWithCap
import com.eifel.bionisation4.common.storage.capability.entity.BioMob
import com.eifel.bionisation4.common.storage.capability.player.BioPlayer
import com.eifel.bionisation4.util.nbt.NBTUtils
import com.eifel.bionisation4.util.translation.TranslationUtils
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundNBT

abstract class AbstractEffect(var effectID: Int, var effectName: String = "Default Effect", var effectType: EffectType = EffectType.COMMON) : INBTSerializable {

    var effectDuration = -1L
    var effectPower = 1

    val effectGenes = mutableListOf<IGene>()

    var isCure = false
    var isInfinite = true//+
    var isHidden = false//+

    var isExpired = false

    var canMutate = false
    var mutationPeriod = ConfigProperties.defaultMutationPeriod.get()

    var canInfectItems = false
    var isAntibioticVulnerable = effectType == EffectType.BACTERIA
    var antibioticResistancePercent = 0.0

    var isSyncable = false

    constructor() : this(0)

    override fun toNBT(): CompoundNBT {
        val nbtData = CompoundNBT()

        nbtData.putInt(InternalConstants.EFFECT_ID_KEY, effectID)
        nbtData.putString(InternalConstants.EFFECT_NAME_KEY, effectName)

        NBTUtils.enumToNBT(nbtData, effectType, InternalConstants.EFFECT_TYPE_KEY)

        nbtData.putLong(InternalConstants.EFFECT_DURATION_KEY, effectDuration)
        nbtData.putInt(InternalConstants.EFFECT_POWER_KEY, effectPower)

        NBTUtils.objectsToNBT(nbtData, effectGenes, InternalConstants.EFFECT_GENES_KEY)

        nbtData.putBoolean(InternalConstants.EFFECT_CURE_KEY, isCure)
        nbtData.putBoolean(InternalConstants.EFFECT_INFINITE_KEY, isInfinite)
        nbtData.putBoolean(InternalConstants.EFFECT_HIDDEN_KEY, isHidden)
        nbtData.putBoolean(InternalConstants.EFFECT_EXPIRED_KEY, isExpired)
        nbtData.putBoolean(InternalConstants.EFFECT_MUTATE_KEY, canMutate)

        nbtData.putInt(InternalConstants.EFFECT_MUTATE_PERIOD_KEY, mutationPeriod)

        nbtData.putBoolean(InternalConstants.EFFECT_INFECT_ITEMS_KEY, canInfectItems)
        nbtData.putBoolean(InternalConstants.EFFECT_ANTIBIOTIC_VULNERABLE_KEY, isAntibioticVulnerable)
        nbtData.putDouble(InternalConstants.EFFECT_ANTIBIOTIC_RESISTANCE_KEY, antibioticResistancePercent)

        nbtData.putBoolean(InternalConstants.EFFECT_SYNCABLE_KEY, isSyncable)

        return nbtData
    }

    override fun fromNBT(nbtData: CompoundNBT) {

        effectID = nbtData.getInt(InternalConstants.EFFECT_ID_KEY)
        effectName = nbtData.getString(InternalConstants.EFFECT_NAME_KEY)

        effectType = NBTUtils.nbtToEnum<EffectType>(nbtData, InternalConstants.EFFECT_TYPE_KEY) ?: EffectType.COMMON

        effectDuration = nbtData.getLong(InternalConstants.EFFECT_DURATION_KEY)
        effectPower = nbtData.getInt(InternalConstants.EFFECT_POWER_KEY)

        NBTUtils.nbtToGenes(nbtData, effectGenes, InternalConstants.EFFECT_GENES_KEY)

        isCure = nbtData.getBoolean(InternalConstants.EFFECT_CURE_KEY)
        isInfinite = nbtData.getBoolean(InternalConstants.EFFECT_INFINITE_KEY)
        isHidden = nbtData.getBoolean(InternalConstants.EFFECT_HIDDEN_KEY)
        isExpired = nbtData.getBoolean(InternalConstants.EFFECT_EXPIRED_KEY)
        canMutate = nbtData.getBoolean(InternalConstants.EFFECT_MUTATE_KEY)

        mutationPeriod = nbtData.getInt(InternalConstants.EFFECT_MUTATE_PERIOD_KEY)

        canInfectItems = nbtData.getBoolean(InternalConstants.EFFECT_INFECT_ITEMS_KEY)
        isAntibioticVulnerable = nbtData.getBoolean(InternalConstants.EFFECT_ANTIBIOTIC_VULNERABLE_KEY)
        antibioticResistancePercent = nbtData.getDouble(InternalConstants.EFFECT_ANTIBIOTIC_RESISTANCE_KEY)

        isSyncable = nbtData.getBoolean(InternalConstants.EFFECT_SYNCABLE_KEY)
    }

    fun recalculatePower(entity: LivingEntity) {
        when(entity){
            is PlayerEntity -> {
                entity.doWithCap<BioPlayer> { cap ->

                }
            }
            else -> {
                entity.doWithCap<BioMob> { cap ->

                }
            }
        }
        //todo recalculate based on immunity system
    }

    fun mutate() {
        if(canMutate){
            //todo add mutation mechanic
        }
    }

    fun getDNA() = effectGenes.joinToString("-", "[", "]", -1, "") { gene -> "${gene.getID()}" }

    fun onExpired(entity: LivingEntity) {
        effectGenes.forEach { gene ->
            gene.clear(entity)
        }
    }

    fun onTick(entity: LivingEntity, isLastTick: Boolean) {
        recalculatePower(entity)
        effectGenes.forEach { gene ->
            gene.perform(entity)
        }
    }

    fun onAttack(victim: LivingEntity, attacker: LivingEntity) {}

    fun onDeath(entity: LivingEntity) {}

    fun perform(entity: LivingEntity){
        if(!entity.level.isClientSide) {
            if (!isInfinite) {
                this.effectDuration--
                if (this.effectDuration <= 0) {
                    onExpired(entity)
                    isExpired = true
                }
            }
            if (!isHidden) {
                if (!isExpired)
                    onTick(entity, false)
                else
                    onTick(entity, true)
            }
        }
    }

    fun isSame(effect: AbstractEffect) = effect.effectID == effectID
    fun isSame(id: Int) = id == effectID
    fun isSame(name: String) = name == effectName

    fun getTranslationName() = TranslationUtils.getTranslatedText("effect", effectName, "name")
}