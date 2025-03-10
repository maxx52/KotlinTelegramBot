import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)
        val updateIdRegex = "\"update_id\":(.+?)".toRegex()
        val updateResult = updateIdRegex.find(updates)
        val groupsOfUpdate = updateResult?.groups
        val updateIdString = groupsOfUpdate?.get(1)?.value
        updateId = updateIdString?.toInt()?.plus(1) ?: 0

        val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()
        val matchResult = messageTextRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value
        println(text)
    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val client = HttpClient.newBuilder().build()
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val update = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response = client.send(update, HttpResponse.BodyHandlers.ofString())
    return response.body()
}
