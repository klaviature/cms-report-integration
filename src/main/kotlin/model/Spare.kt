package org.fomin.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Spare(
    val id: String,
    val name: String,
    val description: String,
    val type: SpareType,
    val status: SpareStatus,
    val price: BigDecimal,
    val quantity: Int,
    val updatedAt: String
)
