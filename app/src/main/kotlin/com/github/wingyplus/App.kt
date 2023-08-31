package com.github.wingyplus

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

@Serializable
data class Query(val query: String)

suspend fun main() {
    val sessionToken = System.getenv("DAGGER_SESSION_TOKEN")
    val sessionPort = System.getenv("DAGGER_SESSION_PORT").toInt()
    val query = """
        query {
            container {
                from(address: "alpine") {
                    withExec(args: ["sleep", "10"]) {
                        stdout
                    }
                }
            }
        }
    """.trimIndent()
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(HttpTimeout)
    }
    val response: String = client.post {
        url {
            protocol = URLProtocol.HTTP
            host = "127.0.0.1"
            port = sessionPort
            path("/query")
        }
        basicAuth(sessionToken, "")
        contentType(ContentType.Application.Json)
        setBody(Query(query))
        timeout {
            requestTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
        }
    }.body()
    println(response)

    client.close()
}
