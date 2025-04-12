package ru.androidsprint.englishtrainer.telegram.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.androidsprint.englishtrainer.telegram.entities.User

@Serializable
data class CallbackQuery(
    val id: String,
    val from: User,
    @SerialName("message")
    val message: Message? = null,
    val data: String,
)