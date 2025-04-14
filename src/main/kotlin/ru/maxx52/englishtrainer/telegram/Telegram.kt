package ru.maxx52.englishtrainer.telegram

import kotlinx.coroutines.runBlocking
import ru.maxx52.englishtrainer.telegram.entities.TelegramUpdates
import ru.maxx52.englishtrainer.telegram.entities.Update
import ru.maxx52.englishtrainer.trainer.LearnWordsTrainer

fun main(args: Array<String>) = runBlocking {
    val botToken = args[0]
    val service = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()
    var lastUpdateId = 0L

    while (true) {
        Thread.sleep(TIME_UPDATE)
        val updates: TelegramUpdates = service.getUpdates(lastUpdateId)

        val firstUpdate: Update = updates.result.firstOrNull() ?: continue
        lastUpdateId = firstUpdate.updateId + 1

        val chatId = firstUpdate.message?.chat?.id
            ?: firstUpdate.callbackQuery?.message?.chat?.id
            ?: continue
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
                val currentWord = trainer.getCurrentQuestion()
                if (currentWord != null) {
                    val correctTranslation = trainer.currentQuestion?.correctAnswer?.translate
                    val responseMessage = "Неправильно! ${currentWord.questionWord} - это $correctTranslation."
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