import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]

    val client = HttpClient.newBuilder().build()
    val update = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
}