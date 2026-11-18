package org.fomin.config

import org.fomin.persistence.SparesTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseConfig {

    fun init(): Database {
        val database = Database.connect(
            url = "jdbc:postgresql://localhost:5432/integration",
            driver = "org.postgresql.Driver",
            user = "integration",
            password = "integration"
        )

        transaction {
            SchemaUtils.createMissingTablesAndColumns(SparesTable)
        }

        return database
    }
}