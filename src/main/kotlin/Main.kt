package org.fomin

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.main.Main
import org.fomin.config.AppConfig
import org.fomin.config.DatabaseConfig
import org.fomin.dto.ExamResultDto
import org.fomin.persistence.SpareStorage
import org.fomin.route.AuthRoute
import org.fomin.route.CmsRoute
import org.fomin.route.ReportRoute
import org.fomin.service.TokenProvider

fun main() {
    val database = DatabaseConfig.init()
    val storage = SpareStorage(database)
    val tokenProvider = TokenProvider()

    val camel = Main()

    camel.configure().apply {
        addRoutesBuilder(CmsRoute(storage))
        addRoutesBuilder(AuthRoute(tokenProvider))
        addRoutesBuilder(ReportRoute(storage))

        addRoutesBuilder(
            object : RouteBuilder() {
                override fun configure() {

                    from("timer:integration?delay=1000&period=${AppConfig.SYNC_INTERVAL_MILLIS}")
                        .routeId("integration-scheduler")
                        .to("direct:integration-process")

                    from("direct:integration-process")
                        .routeId("integration-process")
                        .log("Начало цикла интеграции")

                        // 1. Синхронизация с CMS
                        .to("direct:load-all-cms-spares")

                        // 2. Формирование и отправка отчёта
                        .to("direct:send-report")

                        // 3. Обработка результата
                        .process { exchange ->
                            val result = exchange.message
                                .getBody(ExamResultDto::class.java)

                            if (result != null) {
                                log.info(
                                    "Отчёт отправлен. " +
                                            "Student ID: ${result.studentId}, " +
                                            "Result: ${result.result}, " +
                                            "Period: ${result.period}"
                                )
                            } else {
                                log.warn("Report API вернул пустой результат")
                            }
                        }
                        .log("Цикл интеграции завершён")
                }
            }
        )
    }

    camel.run()
}