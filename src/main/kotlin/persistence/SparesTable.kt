package org.fomin.persistence

import org.fomin.model.SpareStatus
import org.fomin.model.SpareType
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

object SparesTable : Table("spares") {

    val code = varchar("spare_code", 255)
    val name = varchar("spare_name", 255)
    val description = text("spare_description")

    val type = enumerationByName<SpareType>(
        name = "spare_type",
        length = 50
    )

    val status = enumerationByName<SpareStatus>(
        name = "spare_status",
        length = 50
    )

    val price = decimal("price", precision = 12, scale = 2)
    val quantity = integer("quantity")
    val updatedAt = varchar("updated_at", 50)

    override val primaryKey = PrimaryKey(code)
}