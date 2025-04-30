package ru.maxx52.englishtrainer.telegram.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TelegramUpdates(
    val ok: Boolean,
    val result: List<Update> = emptyList(),
    val errorCode: Int? = null,
    val description: String? = null
)

@Serializable
data class SendMessageResponse(
    val ok: Boolean,
    val result: Result,
    val errorCode: Int? = null,
    val description: String? = null
)

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