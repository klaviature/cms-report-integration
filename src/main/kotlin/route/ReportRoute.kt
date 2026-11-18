package org.fomin.route

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.dataformat.csv.CsvDataFormat
import org.fomin.config.AppConfig
import org.fomin.dto.ExamResultDto
import org.fomin.persistence.SpareStorage

class ReportRoute(
    private val spareStorage: SpareStorage
) : RouteBuilder() {

    private val objectMapper = ObjectMapper().registerKotlinModule()

    override fun configure() {
        val csvFormat = CsvDataFormat().apply {
            delimiter = ';'
            recordSeparator = "\n"
            isHeaderDisabled = true
        }

        // Формирование CSV из данных БД
        from("direct:create-report-csv")
            .routeId("create-report-csv")
            .process { exchange ->
                val spares = spareStorage.findAll()

                val rows = spares.map { spare ->
                    listOf(
                        spare.id,
                        spare.name,
                        spare.description,
                        spare.type.name,
                        spare.status.name,
                        spare.price.toInt().toString(),
                        spare.quantity.toString(),
                        spare.updatedAt
                    )
                }

                exchange.message.body = rows
                exchange.setProperty("sparesCount", spares.size)
            }
            .marshal(csvFormat)
            .convertBodyTo(String::class.java)
            .log("CSV generated: \${exchangeProperty.sparesCount} records")

        // Отправка CSV в Report API
        from("direct:send-report")
            .routeId("send-report")

            // Формирование CSV
            .to("direct:create-report-csv")
            .setProperty("reportCsv", body())

            // Авторизация
            .to("direct:get-report-token")
            .setProperty("reportToken", body())

            .setBody(exchangeProperty("reportCsv"))

            // Отправка отчета
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader(
                Exchange.CONTENT_TYPE,
                constant("text/csv; charset=UTF-8")
            )
            .setHeader("Accept", constant("application/json"))
            .setHeader(
                "Authorization",
                simple("Bearer \${exchangeProperty.reportToken}")
            )
            .to("${AppConfig.reportBaseUrl}/csv")

            // Ответ API
            .convertBodyTo(String::class.java)
            .process { exchange ->
                val response = exchange.message.getBody(String::class.java)

                val result = objectMapper.readValue(
                    response,
                    ExamResultDto::class.java
                )

                exchange.message.body = result
            }
            .process { exchange ->
                val result = exchange.message.getBody(ExamResultDto::class.java)!!

                log.info(
                    "Report submitted. " +
                            "Student ID: ${result.studentId}, " +
                            "result: ${result.result}, " +
                            "period: ${result.period}"
                )
            }
    }
}