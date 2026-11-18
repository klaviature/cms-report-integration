package org.fomin.dto

import org.fomin.model.Spare
import org.fomin.model.SpareStatus
import org.fomin.model.SpareType
import java.math.BigDecimal
import java.time.LocalDateTime

data class CmsSpareDto(
    val spareCode: String,
    val spareName: String,
    val spareDescription: String,
    val spareType: SpareType,
    val spareStatus: SpareStatus,
    val price: Double,
    val quantity: Int,
    val updatedAt: String
)

fun CmsSpareDto.toDomain(): Spare =
    Spare(
        id = spareCode,
        name = spareName,
        description = spareDescription,
        type = spareType,
        status = spareStatus,
        price = BigDecimal(price),
        quantity = quantity,
        updatedAt = updatedAt,
    )
