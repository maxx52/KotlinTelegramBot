import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService {
    fun getUpdates(botToken: String, updateId: Int): String {
        val client = HttpClient.newBuilder().build()
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
        val update = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client.send(update, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(botToken: String, chatId: Long, text: String): String {
        val client = HttpClient.newBuilder().build()
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedText"
        val messageRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response = client.send(messageRequest, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}