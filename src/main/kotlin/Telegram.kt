fun main(args: Array<String>) {
    val botToken = args[0]
    val service = TelegramBotService()
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = service.getUpdates(botToken, updateId)

        val updateIdRegex = "\"update_id\":(\\d+)".toRegex()
        val messageTextRegex = "\"text\":\"(.*?)\"".toRegex()
        val chatIdRegex = "\"chat\":\\{\"id\":(\\d+)".toRegex()

        val updateResult = updateIdRegex.find(updates)
        if (updateResult != null) {
            updateId = updateResult.groups[1]?.value?.toInt() ?: updateId
        }

        val messageMatch = messageTextRegex.find(updates)
        val chatIdMatch = chatIdRegex.find(updates)

        if (messageMatch != null && chatIdMatch != null) {
            val text = messageMatch.groups[1]?.value
            val chatId = chatIdMatch.groups[1]?.value?.toLongOrNull()

            if (text != null && chatId != null) {
                println("Received message: $text")
                service.sendMessage(botToken, chatId, text)
            }
        }
    }
}