package com.pedrobneto.mockengine.network

import com.pedrobneto.mock.engine.client.MockEngine
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal object KtorfitProvider {
    private const val BASE_URL = "https://127.0.0.1/"

    fun get(json: Json): Ktorfit = Ktorfit.Builder()
        .baseUrl(BASE_URL)
        .httpClient(MockEngine) {
            engine {
                baseUrl = BASE_URL
                onProvideJson = { json }
            }

            expectSuccess = false

            install(ContentNegotiation) { json(json) }

            defaultRequest { contentType(ContentType.Application.Json) }
        }
        .build()
}
