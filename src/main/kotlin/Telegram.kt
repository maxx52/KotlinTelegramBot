import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }

@Serializable
data class TelegramUpdates(
    val ok: Boolean,
    val result: List<Update>,
)

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null
)

@Serializable
data class Message(
    @SerialName("message_id")
    val messageId: Int,
    val from: User,
    val chat: Chat,
    val date: Int,
    val text: String? = null,
)

@Serializable
data class CallbackQuery(
    val id: String,
    val from: User,
    @SerialName("message")
    val message: Message? = null,
    val data: String,
)

@Serializable
data class User(
    @SerialName("id") val id: Long,
    @SerialName("is_bot") val isBot: Boolean,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("language_code") val languageCode: String? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
    @SerialName("first_name")
    val firstName: String,
    val username: String? = null,
    val type: String,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("text")
    val text: String,
    @SerialName("callback_data")
    val callbackData: String,
)

fun main(args: Array<String>) {
    val botToken = args[0]
    val service = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()
    var lastUpdateId = 0L

    while (true) {
        Thread.sleep(2000)
        val responseString: String? = service.getUpdates(lastUpdateId)

        if (responseString == null) {
            println("No updates received. Waiting for the next cycle.")
            continue
        }

        val response = json.decodeFromString<TelegramUpdates>(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id
        val text = firstUpdate.message?.text
        val data = firstUpdate.callbackQuery?.data

        if (text != null) {
            println("Received message: $text")
            if ((text == "/menu" || text == "/start") && chatId != null) {
                service.sendMenu(json, chatId)
            }
        }

        if (data != null) {
            handleCallbackData(data, chatId, trainer, service)
        }
    }
}

fun handleCallbackData(data: String, chatId: Long?, trainer: LearnWordsTrainer, service: TelegramBotService) {
    if (data.startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
        val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toIntOrNull()

        if (userAnswerIndex != null) {
            val isCorrect = trainer.checkAnswer(userAnswerIndex)

            if (isCorrect) {
                if (chatId != null) {
                    service.sendMessage(json, chatId, "Правильно!")
                }
            } else {
                val correctTranslation =
                    trainer.getNextQuestion()?.correctAnswer?.translate
                val userAnswer = trainer.checkAnswer(userAnswerIndex)
                val responseMessage = "Неправильно! $userAnswer – это $correctTranslation."
                if (chatId != null) {
                    service.sendMessage(json, chatId, responseMessage)
                }
            }

            checkNextQuestionAndSend(trainer, service, chatId)
        } else {
            println("Ошибка: Неверный формат данных ответа.")
        }
    }

    if (data.startsWith(LEARN_WORDS) && chatId != null) {
        checkNextQuestionAndSend(trainer, service, chatId)
    }

    if (data.startsWith(STAT_CLICKED)) {
        trainer.getStatistics()
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    service: TelegramBotService,
    chatId: Long?
) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        if (chatId != null) {
            service.sendMessage(json, chatId, "Все слова в словаре выучены.")
        }
    } else {
        if (chatId != null) {
            service.sendQuestion(json, chatId, question)
        }
    }
}