import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.*
import io.ktor.http.*

const val URL_API = "https://api.telegram.org/bot"
const val LEARN_WORDS = "learn_words_clicked"
const val STAT_CLICKED = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

val json = Json { ignoreUnknownKeys = true }

class TelegramBotService(private val botToken: String) {
    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }
    private val urlSendMessage = "$URL_API$botToken/sendMessage"

    suspend fun getUpdates(lastUpdateId: Long): TelegramUpdates {
        val updatesUrl = "$URL_API$botToken/getUpdates?offset=$lastUpdateId"
        val response: String = client.get(updatesUrl)
        return json.decodeFromString<TelegramUpdates>(response)
    }

    suspend fun sendMessage(chatId: Long, message: String): String? {
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message
        )

        return try {
            val response: String = client.post(urlSendMessage) {
                body = requestBody
                contentType(ContentType.Application.Json)
            }
            response
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
                listOf(listOf(
                    InlineKeyboard(callbackData = LEARN_WORDS, text = "Изучать слова"),
                    InlineKeyboard(callbackData = STAT_CLICKED, text = "Статистика"),
                ))
            )
        )

        return try {
            val response: String = client.post(urlSendMessage) {
                body = requestBody
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
            text = question.correctAnswer.questionWord,
            replyMarkup = ReplyMarkup(
                listOf(question.variants.mapIndexed { index, word ->
                    InlineKeyboard(
                        text = word.translate,
                        callbackData = "${CALLBACK_DATA_ANSWER_PREFIX}${index + 1}"
                    )
                })
            )
        )

        return try {
            val response: String = client.post(urlSendMessage) {
                body = requestBody
                contentType(ContentType.Application.Json)
            }
            handleResponse(response)
        } catch (e: Exception) {
            println("Ошибка отправки вопроса: ${e.message}")
            null
        }
    }

    private fun handleResponse(response: String): String? {
        return try {
            val telegramResponse = json.decodeFromString<TelegramUpdates>(response)
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