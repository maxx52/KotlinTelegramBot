import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers

const val URL_API = "https://api.telegram.org/bot"
const val LEARN_WORDS = "learn_words_clicked"
const val STAT_CLICKED = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

class TelegramBotService(private val botToken: String) {
    private val client: HttpClient = HttpClient.newBuilder().build()
    private val urlSendMessage = "$URL_API$botToken/sendMessage"

    fun getUpdates(updateId: Long): String? {
        val urlGetUpdates = "$URL_API$botToken/getUpdates?offset=$updateId"
        val updateRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        return try {
            val response = client.send(updateRequest, BodyHandlers.ofString())
            handleResponse(response)
        } catch (e: Exception) {
            println("Error getting updates: ${e.message}")
            null
        }
    }

    fun sendMessage(json: Json, chatId: Long, message: String): String? {
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response = client.send(request, BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(json: Json,chatId: Long): String? {
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
        val requestBodyString = json.encodeToString(requestBody)
        val menuRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        return try {
            val response = client.send(menuRequest, BodyHandlers.ofString())
            handleResponse(response)
        } catch (e: Exception) {
            println("Error sending menu: ${e.message}")
            null
        }
    }

    fun sendQuestion(json: Json, chatId: Long, question: Question): String? {
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
        val requestBodyString = json.encodeToString(requestBody)
        val menuRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        return try {
            val httpResponse = client.send(menuRequest, BodyHandlers.ofString())
            handleResponse(httpResponse)
        } catch (e: Exception) {
            println("Error sending menu: ${e.message}")
            null
        }
    }

    private fun handleResponse(response: HttpResponse<String>): String? {
        return if (response.statusCode() == 200) {
            response.body()
        } else {
            println("Error: Received status code ${response.statusCode()}")
            null
        }
    }
}