package com.eifel.bionisation4.common.laboratory.gene.species.potion

import com.eifel.bionisation4.api.constant.InternalConstants
import com.eifel.bionisation4.api.laboratory.species.Gene
import com.eifel.bionisation4.api.laboratory.species.GenePotionEffect

class Jump(): Gene(InternalConstants.GENE_JUMP_ID, "Jump", true) {

    init {
        potions.add(GenePotionEffect(8, 100, 1))
    }

    override fun getCopy() = Jump()
}