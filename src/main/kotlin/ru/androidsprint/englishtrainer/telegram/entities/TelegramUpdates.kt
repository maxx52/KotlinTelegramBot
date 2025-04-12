package ru.androidsprint.englishtrainer.telegram.entities

import kotlinx.serialization.Serializable

@Serializable
data class TelegramUpdates(
    val ok: Boolean,
    val result: List<Update>,
    val errorCode: Int? = null,
    val description: String? = null
)