package org.example.project.data.remote

import io.ktor.client.HttpClient

object AiServiceFactory {

    fun getAiService(httpClient: HttpClient): IAiService {
        // Based on the requirement to use a local, OpenAI-compatible server,
        // we will always return OpenAIApiService.
        // If you had other providers or configurations, you could add logic here
        // to choose the appropriate service.
        return OpenAIApiService(httpClient)
    }
}
