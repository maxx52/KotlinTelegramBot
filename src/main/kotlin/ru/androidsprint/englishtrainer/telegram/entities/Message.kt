package ru.androidsprint.englishtrainer.telegram.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    @SerialName("message_id")
    val messageId: Int,
    val from: User,
    val chat: Chat,
    val date: Int,
    val text: String? = null,
)