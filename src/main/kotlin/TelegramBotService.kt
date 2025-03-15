import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val URL_API = "https://api.telegram.org/bot"

class TelegramBotService(
    private val botToken: String,
) {
    private val client: HttpClient? = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String? {
        val urlGetUpdates = "$URL_API${this.botToken}/getUpdates?offset=$updateId"
        val update = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client?.send(update, HttpResponse.BodyHandlers.ofString())
        return response?.body()
    }

    fun sendMessage(chatId: Long, text: String): String? {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val urlSendMessage = "$URL_API$botToken/sendMessage?chat_id=$chatId&text=$encodedText"
        val messageRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response = client?.send(messageRequest, HttpResponse.BodyHandlers.ofString())
        return response?.body()
    }
}