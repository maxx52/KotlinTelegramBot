fun main(args: Array<String>) {
    val botToken = args[0]
    val service = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()
    var updateId = 0

    val updateIdRegex = "\"update_id\":(\\d+)".toRegex()
    val messageTextRegex = "\"text\":\\s*\"(.*?)\"".toRegex()
    val chatIdRegex = "\"chat\":\\{\"id\":(\\d+)".toRegex()
    val dataRegex = "\"data\":\"(.*?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = service.getUpdates(updateId).toString()
        val updateResult = updateIdRegex.find(updates)

        if (updateResult != null) {
            updateId = updateResult.groups[1]?.value?.toInt() ?: updateId
        }

        val messageMatch = messageTextRegex.find(updates)
        val chatIdMatch = chatIdRegex.find(updates)
        val dataMatch = dataRegex.find(updates)

        if (messageMatch != null && chatIdMatch != null) {
            val text = messageMatch.groups[1]?.value
            val chatId = chatIdMatch.groups[1]?.value?.toLongOrNull() ?: continue
            val data = dataMatch?.groups?.get(1)?.value

            if (text != null) {
                println("Received message: $text")
                service.sendMessage(chatId, text)
            }

            if (text == "menu") {
                println("Received message: $text")
                service.sendMenu(chatId)
            }

            if (data == STAT_CLICKED) {
                println("Received message: $text")
                val statistics = trainer.getStatistics()
                service.sendMessage(chatId, "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%")
            }

            if (data == LEARN_WORDS) {
                println("Received message: $text")
                if (text != null) {
                    checkNextQuestionAndSend(trainer, service, chatId)
                }
            }
        }
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
        telegramBotService.sendQuestion(chatId, question)
    }
}