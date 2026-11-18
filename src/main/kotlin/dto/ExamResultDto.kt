package org.fomin.dto

data class ExamResultDto(
    val studentId: Int,
    val result: Int,
    val lastSyncAt: String,
    val lastSyncMessage: String,
    val period: Int
)
