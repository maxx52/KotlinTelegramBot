package ru.maxx52.englishtrainer.telegram

import kotlinx.serialization.json.Json
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import ru.maxx52.englishtrainer.telegram.entities.InlineKeyboard
import ru.maxx52.englishtrainer.telegram.entities.ReplyMarkup
import ru.maxx52.englishtrainer.telegram.entities.SendMessageRequest
import ru.maxx52.englishtrainer.telegram.entities.TelegramUpdates
import ru.maxx52.englishtrainer.trainer.model.Question

const val URL_API = "https://api.telegram.org/bot"
const val LEARN_WORDS = "learn_words_clicked"
const val STAT_CLICKED = "statistics_clicked"
const val NULL_DICTIONARY = "null_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val DEFAULT_LEARNING_WORDS = 3
const val TIME_UPDATE = 2000L

class TelegramBotService(
    private val botToken: String
) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }
    private val urlSendMessage = "$URL_API$botToken/sendMessage"

    suspend fun getUpdates(lastUpdateId: Long): TelegramUpdates {
        val updatesUrl = "$URL_API$botToken/getUpdates?offset=$lastUpdateId"

        return try {
            client.get(updatesUrl).body()
        } catch (e: Exception) {
            println("Ошибка получения обновлений: ${e.message}")
            TelegramUpdates(
                ok = false,
            )
        }
    }

    suspend fun sendMessage(chatId: Long, message: String): String? {
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message
        )

        return try {
            val response = client.post(urlSendMessage) {
                setBody(requestBody)
                contentType(ContentType.Application.Json)
            }
            handleResponse(response)
        } catch (e: Exception) {
            println("Ошибка отправки сообщения: ${e.message}")
            null
        }
    }

    suspend fun sendMenu(chatId: Long): String? {
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(callbackData = LEARN_WORDS, text = "Изучать слова"),
                    ),
                    listOf(
                        InlineKeyboard(callbackData = STAT_CLICKED, text = "Статистика"),
                    ),
                    listOf(
                        InlineKeyboard(callbackData = NULL_DICTIONARY, text = "Обнулить прогресс"),
                    )
                )
            )
        )

        return try {
            val response = client.post(urlSendMessage) {
                setBody(requestBody)
                contentType(ContentType.Application.Json)
            }
            handleResponse(response)
        } catch (e: Exception) {
            println("Error sending menu: ${e.message}")
            null
        }
    }

    suspend fun sendQuestion(chatId: Long, question: Question): String? {
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                question.variants.map { word ->
                    listOf(
                        InlineKeyboard(
                            text = word.translate,
                            callbackData = "$CALLBACK_DATA_ANSWER_PREFIX${question.variants.indexOf(word) + 1}"
                        )
                    )
                }
            )
        )

        return try {
            val response: HttpResponse = client.post(urlSendMessage) {
                setBody(requestBody)
                contentType(ContentType.Application.Json)
            }
            handleResponse(response)
        } catch (e: Exception) {
            println("Ошибка отправки вопроса: ${e.message}")
            null
        }
    }


    private suspend fun handleResponse(response: HttpResponse): String? {
        return try {
            val telegramResponse = response.body<TelegramUpdates>()
            if (telegramResponse.ok) {
                telegramResponse.result.let {
                    "Сообщение отправлено успешно"
                }
            } else {
                println("Ошибка в ответе от Telegram: ${telegramResponse.errorCode} ${telegramResponse.description}")
                null
            }
        } catch (e: Exception) {
            println("Ошибка обработки ответа: ${e.message}")
            null
        }
    }
}