package org.fomin.route

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.jackson.JacksonDataFormat
import org.fomin.config.AppConfig
import org.fomin.dto.CmsSpareDto
import org.fomin.dto.toDomain
import org.fomin.persistence.SpareStorage
import java.util.Base64

class CmsRoute(
    private val spareStorage: SpareStorage,
) : RouteBuilder() {

    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val pageSize = 10

    override fun configure() {
        val cmsSpareListFormat = JacksonDataFormat(
            objectMapper,
            CmsSpareDto::class.java
        ).apply {
            useList()
        }

        val credentials = "${AppConfig.cmsUsername}:${AppConfig.cmsPassword}"
        val encodedCredentials = Base64.getEncoder()
            .encodeToString(credentials.toByteArray(Charsets.UTF_8))

        // Получение одной страницы
        from("direct:load-cms-page")
            .routeId("load-cms-page")
            .setHeader(Exchange.HTTP_METHOD, constant("GET"))
            .setHeader(
                Exchange.HTTP_QUERY,
                simple("page=\${header.page}&size=$pageSize")
            )
            .setHeader(
                "Authorization",
                constant("Basic $encodedCredentials")
            )
            .to("${AppConfig.cmsBaseUrl}/spares")
            .unmarshal(cmsSpareListFormat)

        // Полная синхронизация
        from("direct:load-all-cms-spares")
            .routeId("load-all-cms-spares")

            .setProperty("page", constant(0))
            .setProperty("hasMorePages", constant(true))
            .setProperty("pagesCount", constant(0))

            .process { exchange ->
                exchange.setProperty(
                    "allSpares",
                    mutableListOf<CmsSpareDto>()
                )
            }

            // Получение всех данных со всех страниц
            .loopDoWhile(simple("\${exchangeProperty.hasMorePages}"))
            .setHeader("page", exchangeProperty("page"))
            .to("direct:load-cms-page")

            .process { exchange ->
                @Suppress("UNCHECKED_CAST")
                val currentPage =
                    exchange.message.body as List<CmsSpareDto>

                @Suppress("UNCHECKED_CAST")
                val allSpares =
                    exchange.getProperty("allSpares") as MutableList<CmsSpareDto>

                allSpares.addAll(currentPage)

                exchange.setProperty(
                    "hasMorePages",
                    currentPage.size == pageSize
                )

                val page = exchange.getProperty("page", Int::class.java)!!

                exchange.setProperty("page", page + 1)
                exchange.setProperty(
                    "pagesCount",
                    exchange.getProperty("pagesCount", Int::class.java)!! + 1
                )
            }
            .end()

            // Сохранение в БД
            .setBody(exchangeProperty("allSpares"))

            .process { exchange ->
                @Suppress("UNCHECKED_CAST")
                val dtos = exchange.message.body as List<CmsSpareDto>

                val spares = dtos.map { it.toDomain() }

                spareStorage.upsertAll(spares)

                exchange.message.body = spares

                exchange.setProperty("sparesCount", spares.size)
            }

            .log(
                "CMS synchronization completed. " +
                        "Pages: \${exchangeProperty.pagesCount}, " +
                        "spares received and saved: \${exchangeProperty.sparesCount}"
            )
    }
}