import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun main(args: Array<String>) {
    val botToken = args[0]
    val service = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()
    var updateId = 0

    val updateIdRegex = """"update_id":(\d+)""".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String? = service.getUpdates(updateId)

        if (updates == null) {
            println("No updates received. Waiting for the next cycle.")
            continue
        }

        val telegramUpdates = json.decodeFromString<TelegramUpdates>(updates)

        for (update in telegramUpdates.result) {
            val chatId = update.message?.chat?.id ?: continue
            val text = update.message.text
            val data = update.callbackQuery?.data

            // Обработка текстового сообщения
            if (text != null) {
                println("Received message: $text")
                if (text == "menu") {
                    println("Menu command received.")
                    service.sendMenu(chatId)
                }
            }

            if (data != null) {
                handleCallbackData(data, chatId, trainer, service)
            }
        }

        val updateResult = updateIdRegex.find(updates)
        if (updateResult != null) {
            updateId = (updateResult.groups[1]?.value?.toInt()?.plus(1)) ?: 0
        }
    }
}

fun handleCallbackData(data: String, chatId: Long?, trainer: LearnWordsTrainer, service: TelegramBotService) {
    if (data.startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
        val answerIndexStr = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX)
        val userAnswerIndex = answerIndexStr.toIntOrNull()

        if (userAnswerIndex != null) {
            val isCorrect = trainer.checkAnswer(userAnswerIndex)

            if (isCorrect) {
                if (chatId != null) {
                    service.sendMessage(chatId, "Правильно!")
                }
            } else {
                val correctTranslation =
                    trainer.getNextQuestion()?.correctAnswer?.translate
                val userAnswer = trainer.checkAnswer(userAnswerIndex)
                val responseMessage = "Неправильно! $userAnswer – это $correctTranslation."
                if (chatId != null) {
                    service.sendMessage(chatId, responseMessage)
                }
            }

            checkNextQuestionAndSend(trainer, service, chatId)
        } else {
            println("Ошибка: Неверный формат данных ответа.")
        }
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long?
) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        if (chatId != null) {
            telegramBotService.sendMessage(chatId, "Все слова в словаре выучены.")
        }
    } else {
        if (chatId != null) {
            telegramBotService.sendQuestion(chatId, question)
        }
    }
}