package me.lasta.studyktor

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class UserArticle(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

@KtorExperimentalAPI
fun main() = runBlocking {

    // https://ktor.io/docs/http-client-engines.html
    val cioClient = HttpClient(CIO) {
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

    val userArticle: UserArticle = cioClient.use { client ->
        // https://jsonplaceholder.typicode.com/
        client.get("http://jsonplaceholder.typicode.com/posts/1")
    }
    println(Json.encodeToString(UserArticle.serializer(), userArticle))
}


