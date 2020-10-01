package me.lasta.studyktor

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.curl.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.system.exitProcess

@Serializable
data class UserArticle(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

@KtorExperimentalAPI
fun main() = runBlocking {
//    withCioClient()
    withCurlClient()
}

@KtorExperimentalAPI
suspend fun withCioClient() {
    // https://ktor.io/docs/http-client-engines.html
    val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        engine {
            maxConnectionsCount = 1000
            endpoint {
                maxConnectionsPerRoute = 100
                pipelineMaxSize = 20
                keepAliveTime = 5000
                connectTimeout = 5000
                connectRetryAttempts = 5
            }
            https { serverName = "study.lasta.me" }
        }
    }
    client.use { test(it) }
    test(client)
}

suspend fun withCurlClient() {
    // https://ktor.io/docs/http-client-engines.html
    val client = HttpClient(Curl) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }
    client.use { test(it) }
}

suspend fun test(client: HttpClient) {
    // HTTP
    val httpUrl = "http://jsonplaceholder.typicode.com/posts/1"
    println("Test to request: $httpUrl")
    val userArticle: UserArticle = try {
        // https://jsonplaceholder.typicode.com/
        client.get(httpUrl)
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(1)
    }
    println(Json.encodeToString(UserArticle.serializer(), userArticle))

    // HTTPS
    val httpsUrl = "https://jsonplaceholder.typicode.com/posts/1"
    println("Test to request $httpsUrl")
    val userArticleHttps: UserArticle = try {
        client.get(httpsUrl)
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(1)
    }
    println(Json.encodeToString(UserArticle.serializer(), userArticleHttps))
}
