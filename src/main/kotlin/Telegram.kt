import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TelegramUpdates(
    val ok: Boolean,
    val result: List<Update>,
    val errorCode: Int? = null,
    val description: String? = null
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

fun main(args: Array<String>) = runBlocking {
    val botToken = args[0]
    val service = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()
    var lastUpdateId = 0L

    while (true) {
        Thread.sleep(2000)
        val updates = service.getUpdates(lastUpdateId)

        val firstUpdate = updates.result.firstOrNull() ?: continue
        lastUpdateId = firstUpdate.updateId + 1

        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id ?: continue
        val text = firstUpdate.message?.text
        val data = firstUpdate.callbackQuery?.data

        if (text != null) {
            println("Received message: $text")
            if (text == "/menu" || text == "/start") {
                service.sendMenu(chatId)
            }
        }

        if (data != null) {
            handleCallbackData(data, chatId, trainer, service)
        }
    }
}

suspend fun handleCallbackData(
    data: String,
    chatId: Long,
    trainer: LearnWordsTrainer,
    service: TelegramBotService
) {
    if (data.startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
        val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toIntOrNull()

        if (userAnswerIndex != null) {
            val isCorrect = trainer.checkAnswer(userAnswerIndex)

            if (isCorrect) {
                service.sendMessage(chatId, "Правильно!")
            } else {
                val currentWord = trainer.getCurrentQuestion() // Получаем текущее слово
                if (currentWord != null) {
                    val correctTranslation = currentWord.translate  // Достаем перевод
                    val userAnswer = trainer.checkAnswer(userAnswerIndex) // Получаем ответ пользователя
                    val responseMessage = "Неправильно! $userAnswer – это $correctTranslation."
                    service.sendMessage(chatId, responseMessage)
                } else {
                    service.sendMessage(chatId, "Ошибка: Неверный вопрос.")
                }
            }

            checkNextQuestionAndSend(trainer, service, chatId)
        } else {
            println("Ошибка: Неверный формат данных ответа.")
        }
    }

    if (data.startsWith(LEARN_WORDS)) {
        checkNextQuestionAndSend(trainer, service, chatId)
    }

    if (data.startsWith(STAT_CLICKED)) {
        val statistics = trainer.getStatistics()
        val message = "Выучено ${statistics.learnedCount.size} из ${statistics.totalCount} слов | ${statistics.percent}%"
        service.sendMessage(chatId, message)
    }
}

suspend fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    service: TelegramBotService,
    chatId: Long,
) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        service.sendMessage(chatId, "Все слова в словаре выучены.")
    } else {
        service.sendQuestion(chatId, question)
    }
}