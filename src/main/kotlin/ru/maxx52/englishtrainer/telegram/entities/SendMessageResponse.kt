package ru.maxx52.englishtrainer.telegram.entities

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageResponse(
    val ok: Boolean,
    val result: Result,
    val errorCode: Int? = null,
    val description: String? = null
)