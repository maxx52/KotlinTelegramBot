package ru.maxx52.englishtrainer.telegram.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CallbackQuery(
    val id: String,
    val from: User,
    @SerialName("message")
    val message: Message? = null,
    val data: String,
)