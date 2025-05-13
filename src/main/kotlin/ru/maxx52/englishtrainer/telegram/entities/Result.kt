package ru.maxx52.englishtrainer.telegram.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Result(
    @SerialName("message_id")
    val messageId: Long,
    val chat: Chat?,
    val date: Long?,
    val text: String?,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)