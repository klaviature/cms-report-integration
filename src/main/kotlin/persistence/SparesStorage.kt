package org.fomin.persistence

import org.fomin.model.Spare
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

class SpareStorage(
    private val database: Database,
) {

    fun upsertAll(spares: List<Spare>) {
        transaction(database) {
            spares.forEach { spare ->
                SparesTable.upsert {
                    it[code] = spare.id
                    it[name] = spare.name
                    it[description] = spare.description
                    it[type] = spare.type
                    it[status] = spare.status
                    it[price] = spare.price
                    it[quantity] = spare.quantity
                    it[updatedAt] = spare.updatedAt
                }
            }
        }
    }

    fun findAll(): List<Spare> =
        transaction(database) {
            SparesTable
                .selectAll()
                .map { row ->
                    Spare(
                        id = row[SparesTable.code],
                        name = row[SparesTable.name],
                        description = row[SparesTable.description],
                        type = row[SparesTable.type],
                        status = row[SparesTable.status],
                        price = row[SparesTable.price],
                        quantity = row[SparesTable.quantity],
                        updatedAt = row[SparesTable.updatedAt],
                    )
                }
        }
}