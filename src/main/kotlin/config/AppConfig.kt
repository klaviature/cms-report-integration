package org.fomin.config

object AppConfig {
    val cmsBaseUrl: String =
        requireNotNull(System.getenv("CMS_BASE_URL")) {
            "CMS_BASE_URL is not set"
        }

    val cmsUsername: String =
        requireNotNull(System.getenv("CMS_USERNAME")) {
            "CMS_USERNAME is not set"
        }

    val cmsPassword: String =
        requireNotNull(System.getenv("CMS_PASSWORD")) {
            "CMS_PASSWORD is not set"
        }

    val authBaseUrl: String =
        requireNotNull(System.getenv("AUTH_BASE_URL")) {
            "AUTH_BASE_URL is not set"
        }

    val reportBaseUrl: String =
        requireNotNull(System.getenv("REPORT_BASE_URL")) {
            "REPORT_BASE_URL is not set"
        }

    private const val SYNC_INTERVAL_MINUTES: Int = 1

    const val SYNC_INTERVAL_MILLIS: Long = SYNC_INTERVAL_MINUTES.toLong() * 60 * 1000
}