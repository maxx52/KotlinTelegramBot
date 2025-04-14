package ru.maxx52.englishtrainer.telegram.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InlineKeyboard(
    @SerialName("text")
    val text: String,
    @SerialName("callback_data")
    val callbackData: String,
)