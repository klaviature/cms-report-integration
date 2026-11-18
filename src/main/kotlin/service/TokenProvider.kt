package org.fomin.service

import org.fomin.dto.TokenDto
import java.time.Instant

class TokenProvider {

    @Volatile
    private var cachedToken: TokenDto? = null

    @Synchronized
    fun getValidToken(): String? {
        val token = cachedToken ?: return null

        val now = Instant.now().epochSecond
        val safetyMargin = 30L

        return if (token.validUntil > now + safetyMargin) {
            token.token
        } else {
            null
        }
    }

    @Synchronized
    fun saveToken(token: TokenDto) {
        cachedToken = token
    }
}