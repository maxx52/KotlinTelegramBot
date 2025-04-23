package ru.maxx52.englishtrainer.telegram

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ru.maxx52.englishtrainer.telegram.entities.TelegramUpdates
import ru.maxx52.englishtrainer.telegram.entities.Update
import ru.maxx52.englishtrainer.trainer.LearnWordsTrainer

fun main(args: Array<String>) = runBlocking {
    val botToken = args[0]
    val service = TelegramBotService(botToken)
    val trainers = HashMap<Long, LearnWordsTrainer>()
    var lastUpdateId = 0L

    while (true) {
        delay(TIME_UPDATE)
        val updates: TelegramUpdates = service.getUpdates(lastUpdateId)
        if (updates.result.isEmpty()) continue
        val sortedUpdates = updates.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, service, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

suspend fun handleUpdate(
    update: Update,
    service: TelegramBotService,
    trainers: HashMap<Long, LearnWordsTrainer>
) {
    val chatId: Long = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return

    val text = update.message?.text
    val data = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    when {
        text != null -> {
            println("Received message: $text")
            when (text) {
                "/menu", "/start" -> {
                    service.sendMenu(chatId)
                }
                "/learn_words" -> {
                    checkNextQuestionAndSend(trainer, service, chatId)
                }
                "/statistics" -> {
                    val statistics = trainer.getStatistics()
                    service.sendMessage(chatId, "Выучено ${statistics.learnedCount.size} из ${statistics.totalCount} слов | ${statistics.percent}%")
                }
                "/restart" -> {
                    trainer.restartLearning()
                    service.sendMessage(chatId, "Прогресс обновлён")
                }
                else -> {
                    println("Неизвестная команда: $text")
                }
            }
        }
        data != null -> {
            println("Received callback data: $data")
            handleCallbackData(data, chatId, trainer, service)
        }
        else -> false
    }
}

suspend fun handleCallbackData(
    data: String,
    chatId: Long,
    trainer: LearnWordsTrainer,
    service: TelegramBotService
) {
    when {
        data.startsWith(CALLBACK_DATA_ANSWER_PREFIX) -> {
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
                        service.sendMessage(chatId, "Ошибка: Неверный вопрос")
                    }
                }

                checkNextQuestionAndSend(trainer, service, chatId)
            } else {
                println("Ошибка: Неверный формат данных ответа")
            }
        }
        data.startsWith(LEARN_WORDS) -> {
            checkNextQuestionAndSend(trainer, service, chatId)
        }
        data.startsWith(STAT_CLICKED) -> {
            val statistics = trainer.getStatistics()
            service.sendMessage(chatId, "Выучено ${statistics.learnedCount.size} из ${statistics.totalCount} слов | ${statistics.percent}%")
        }
        data.startsWith(NULL_DICTIONARY) -> {
            trainer.restartLearning()
            service.sendMessage(chatId, "Прогресс обновлён")
        }
        else -> service.sendMessage(chatId, "Неизвестная команда!")
    }
}

suspend fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    service: TelegramBotService,
    chatId: Long,
) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        service.sendMessage(chatId, "Все слова в словаре выучены")
    } else {
        service.sendQuestion(chatId, question)
    }
}