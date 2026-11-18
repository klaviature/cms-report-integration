package org.fomin.route

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.jackson.JacksonDataFormat
import org.fomin.config.AppConfig
import org.fomin.dto.TokenDto
import org.fomin.dto.TokenRequestDto
import org.fomin.service.TokenProvider

class AuthRoute(
    private val tokenProvider: TokenProvider,
) : RouteBuilder() {

    private val objectMapper = ObjectMapper().registerKotlinModule()

    override fun configure() {
        val tokenFormat = JacksonDataFormat(
            objectMapper,
            TokenDto::class.java
        )

        // Запрос нового токена
        from("direct:request-auth-token")
            .routeId("request-auth-token")
            .process { exchange ->
                exchange.message.body = TokenRequestDto(
                    username = AppConfig.cmsUsername,
                    password = AppConfig.cmsPassword
                )
            }
            .marshal().json()
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .to("${AppConfig.authBaseUrl}/token")
            .unmarshal(tokenFormat)
            .log("A new authentication token has been received")

        // Получение действующего токена
        from("direct:get-report-token")
            .routeId("get-report-token")
            .process { exchange ->
                val token = tokenProvider.getValidToken()

                exchange.setProperty("tokenMissing", token == null)

                if (token != null) {
                    exchange.message.body = token
                }
            }
            .choice()
            .`when`(exchangeProperty("tokenMissing").isEqualTo(true))
            .log("Requesting a new authentication token")
            .to("direct:request-auth-token")
            .process { exchange ->
                val token = exchange.message.getBody(TokenDto::class.java)!!

                tokenProvider.saveToken(token)
                exchange.message.body = token.token

            }
            .otherwise()
            .log("Using cached authentication token")
            .end()
    }
}