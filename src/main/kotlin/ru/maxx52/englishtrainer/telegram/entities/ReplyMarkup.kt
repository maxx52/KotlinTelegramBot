package ru.maxx52.englishtrainer.telegram.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)