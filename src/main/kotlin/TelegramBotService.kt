import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers

const val URL_API = "https://api.telegram.org/bot"
const val LEARN_WORDS = "learn_words_clicked"
const val STAT_CLICKED = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

class TelegramBotService(
    private val botToken: String,
) {
    private val client: HttpClient = HttpClient.newBuilder().build()
    private val urlSendMessage = "$URL_API$botToken/sendMessage"

    fun getUpdates(updateId: Int): String? {
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

    fun sendMessage(chatId: Long, text: String): String? {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val urlSendMessage = "$URL_API$botToken/sendMessage?chat_id=$chatId&text=$encodedText"
        val messageRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()

        return try {
            val response = client.send(messageRequest, BodyHandlers.ofString())
            handleResponse(response)
        } catch (e: Exception) {
            println("Error sending message: ${e.message}")
            null
        }
    }

    fun sendMenu(chatId: Long): String? {
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучить слова",
                                "callback_data": "$LEARN_WORDS"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "$STAT_CLICKED"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

        val menuRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()

        return try {
            val response = client.send(menuRequest, BodyHandlers.ofString())
            handleResponse(response)
        } catch (e: Exception) {
            println("Error sending menu: ${e.message}")
            null
        }
    }

    private fun sendQuestion(botService: TelegramBotService, chatId: Long, question: Question): String? {
        val inlineKeyboard = question.variants.mapIndexed { index, option ->
            """
            {
                "text": "${option.translate}",
                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX}${index + 1}"
            }
            """.trimIndent()
        }

        val requestBody = """
        {
        	"chat_id": $chatId,
        	"text": "${question.correctAnswer.questionWord}",
        	"reply_markup": {
                "inline_keyboard": [$inlineKeyboard]
        	}
        }
        """.trimIndent()

        val menuRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        return try {
            val httpResponse = client.send(menuRequest, BodyHandlers.ofString())
            handleResponse(httpResponse)
        } catch (e: Exception) {
            println("Error sending menu: ${e.message}")
            null
        }
    }

    fun checkNextQuestionAndSend(
        trainer: LearnWordsTrainer,
        telegramBotService: TelegramBotService,
        chatId: Long,
    ) {
        val question = trainer.getNextQuestion()
        if (question == null) {
            telegramBotService.sendMessage(chatId, "Все слова в словаре выучены.")
        } else {
            sendQuestion(telegramBotService, chatId, question)
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