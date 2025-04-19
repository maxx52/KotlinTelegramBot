package ru.maxx52.englishtrainer.telegram.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val username: String? = null,
    val type: String,
)