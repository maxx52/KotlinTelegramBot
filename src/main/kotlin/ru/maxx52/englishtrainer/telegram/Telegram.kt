package ru.maxx52.englishtrainer.telegram

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ru.maxx52.englishtrainer.telegram.entities.TelegramUpdates
import ru.maxx52.englishtrainer.telegram.entities.Update
import ru.maxx52.englishtrainer.trainer.DatabaseUserDictionary
import ru.maxx52.englishtrainer.trainer.FileUserDictionary
import ru.maxx52.englishtrainer.trainer.LearnWordsTrainer

fun main(args: Array<String>) = runBlocking {
    val botToken = args[0]
    val service = TelegramBotService(botToken)
    val trainers = HashMap<Long, DatabaseUserDictionary>()
    var lastUpdateId = 0L

    while (true) {
        delay(TIME_UPDATE)
        val updates: TelegramUpdates = service.getUpdates(lastUpdateId)
        if (updates.result.isEmpty()) continue
        val sortedUpdates = updates.result.sortedBy { it.updateId }
        sortedUpdates.forEach {
            handleUpdate(it, service, trainers, learnWordsTrainer = LearnWordsTrainer())
        }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

suspend fun handleUpdate(
    update: Update,
    service: TelegramBotService,
    trainers: HashMap<Long, DatabaseUserDictionary>,
    learnWordsTrainer: LearnWordsTrainer,
) {
    val chatId: Long = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val text = update.message?.text
    val data = update.callbackQuery?.data
    val userId = update.message?.from?.id?.toInt() ?: 0

    val trainer = trainers[chatId] ?: DatabaseUserDictionary()
    trainers[chatId] = trainer
    val statistics = FileUserDictionary().getStatistics()

    when {
        text != null -> {
            println("Received message: $text")
            when (text) {
                "/menu", "/start" -> {
                    service.sendMenu(chatId)
                }
                "/learn_words" -> {
                    checkNextQuestionAndSend(
                        trainer,
                        service,
                        chatId,
                        update,
                        statistics.learnedCount.size,
                        countOfQuestionWords = trainer.getNumOfLearnedWords(userId)
                    )
                }
                "/statistics" -> {
                    service.sendMessage(chatId, "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%")
                }
                "/restart" -> {
                    trainer.restartLearning(userId)
                    service.sendMessage(chatId, "Прогресс обновлён")
                }
                else -> {
                    service.sendMessage(chatId, "Неизвестная команда")
                }
            }
        }
        data != null -> {
            println("Received callback data: $data")
            handleCallbackData(data, chatId, learnWordsTrainer, service, file = DatabaseUserDictionary(), update)
        }
    }
}

suspend fun handleCallbackData(
    data: String,
    chatId: Long,
    trainer: LearnWordsTrainer,
    service: TelegramBotService,
    file: DatabaseUserDictionary,
    update: Update,
) {
    when {
        data.startsWith(CALLBACK_DATA_ANSWER_PREFIX) -> {
            val userAnswerIndex = data
                .substringAfter(CALLBACK_DATA_ANSWER_PREFIX)
                .toIntOrNull()

            if (userAnswerIndex != null) {
                val isCorrect = trainer.checkAnswer(userAnswerIndex)

                if (isCorrect) {
                    service.sendMessage(chatId, "Правильно!")
                } else {
                    val currentWord = trainer.getCurrentQuestion()
                    if (currentWord != null) {
                        val correctTranslation = trainer.currentQuestion?.correctAnswer?.translate
                        val responseMessage = "Неправильно! ${currentWord.original} - это $correctTranslation."
                        service.sendMessage(chatId, responseMessage)
                    } else {
                        service.sendMessage(chatId, "Ошибка: Неверный вопрос")
                    }
                }
                val statistics = FileUserDictionary().getStatistics()
                val userId = (update.message?.from?.id)?.toInt() ?: 0
                checkNextQuestionAndSend(
                    file,
                    service,
                    chatId,
                    update,
                    statistics.learnedCount.size,
                    countOfQuestionWords = file.getNumOfLearnedWords(userId)
                )
            } else {
                println("Ошибка: Неверный формат данных ответа")
            }
        }
        data.startsWith(LEARN_WORDS) -> {
            val userId = update.message?.from?.id?.toInt() ?: 0
            val learnedCount = file.getLearnedWords(userId).size
            checkNextQuestionAndSend(file, service, chatId, update, learnedCount, file.getNumOfLearnedWords(userId))
        }
        data.startsWith(STAT_CLICKED) -> {
            val statistics = FileUserDictionary().getStatistics()
            service.sendMessage(chatId, "Выучено ${statistics.learnedCount.size} из ${statistics.totalCount} слов | ${statistics.percent}%")
        }
        data.startsWith(NULL_DICTIONARY) -> {
            val userId = update.message?.from?.id?.toInt() ?: 0
            file.restartLearning(userId)
            service.sendMessage(chatId, "Прогресс обновлён")
        }
        else -> service.sendMessage(chatId, "Неизвестная команда!")
    }
}

suspend fun checkNextQuestionAndSend(
    trainer: DatabaseUserDictionary,
    service: TelegramBotService,
    chatId: Long,
    update: Update,
    learnedCount: Int,
    countOfQuestionWords: Int
) {
    val userId = update.message?.from?.id?.toInt() ?: 0
    val question = trainer.getNextQuestion(userId, learnedCount, countOfQuestionWords)

    if (question == null) {
        service.sendMessage(chatId, "Все слова в словаре выучены")
    } else {
        service.sendQuestion(chatId, question)
    }
}