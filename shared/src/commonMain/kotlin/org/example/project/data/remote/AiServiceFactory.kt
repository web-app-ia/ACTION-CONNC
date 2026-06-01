package org.example.project.data.remote

import io.ktor.client.HttpClient
import org.example.project.domain.ServerConfig

object AiServiceFactory {

    private var currentService: OpenAIApiService? = null

    fun getAiService(httpClient: HttpClient): OpenAIApiService {
        val service = OpenAIApiService(httpClient)
        currentService = service
        return service
    }

    fun updateConfig(httpClient: HttpClient, newConfig: ServerConfig): OpenAIApiService {
        val service = currentService ?: OpenAIApiService(httpClient, newConfig)
        service.updateConfig(newConfig)
        currentService = service
        return service
    }
}
