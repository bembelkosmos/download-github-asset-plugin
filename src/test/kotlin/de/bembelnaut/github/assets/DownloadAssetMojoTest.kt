package de.bembelnaut.github.assets

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.core5.http.ClassicHttpResponse
import org.apache.hc.core5.http.HttpEntity
import org.apache.hc.core5.http.io.HttpClientResponseHandler
import org.apache.hc.core5.http.io.entity.StringEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.io.File

/**
 * Test class for mojo.
 *
 * Currently, three tests are implemented.
 *
 */
class DownloadAssetMojoTest {

    private lateinit var downloadAssetMojo: DownloadAssetMojo
    private lateinit var httpClient: CloseableHttpClient
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        downloadAssetMojo = DownloadAssetMojo()
        httpClient = mock(CloseableHttpClient::class.java)
        objectMapper = ObjectMapper()

        downloadAssetMojo.githubToken = "dummy_token"
        downloadAssetMojo.repoOwner = "dummy_owner"
        downloadAssetMojo.repoName = "dummy_repo"
        downloadAssetMojo.version = "v1.0.0"
        downloadAssetMojo.assetName = "dummy_asset.zip"
        downloadAssetMojo.outputFile = "output/dummy_asset.zip"
    }

    @Test
    fun `fetchAssetsUrlOfRelease should return assets URL when response is valid`() {
        val response = mock(ClassicHttpResponse::class.java)
        val entity = StringEntity("""{"assets_url": "https://api.github.com/repos/dummy_owner/dummy_repo/releases/assets"}""")

        `when`(response.entity).thenReturn(entity)
        `when`(httpClient.execute(any(HttpGet::class.java), any(HttpClientResponseHandler::class.java)))
            .thenAnswer { invocation ->
                val handler = invocation.getArgument<HttpClientResponseHandler<String>>(1)
                handler.handleResponse(response)
            }

        val result = downloadAssetMojo.fetchAssetsUrlOfRelease(httpClient)
        assertEquals("https://api.github.com/repos/dummy_owner/dummy_repo/releases/assets", result)
    }

    @Test
    fun `fetchAssetUrl should return asset URL when asset is found`() {
        val response = mock(ClassicHttpResponse::class.java)
        val entity = StringEntity("""[{"name": "test-asset", "url": "https://api.github.com/repos/dummy_owner/dummy_repo/releases/assets/123"}]""")

        `when`(response.entity).thenReturn(entity)
        `when`(httpClient.execute(any(HttpGet::class.java), any(HttpClientResponseHandler::class.java)))
            .thenAnswer { invocation ->
                val handler = invocation.getArgument<HttpClientResponseHandler<String>>(1)
                handler.handleResponse(response)
            }

        downloadAssetMojo.assetName = "test-asset"

        val result = downloadAssetMojo.fetchAssetUrl(httpClient, "https://api.github.com/repos/dummy_owner/dummy_repo/releases/assets")
        assertEquals("https://api.github.com/repos/dummy_owner/dummy_repo/releases/assets/123", result)
    }

    @Test
    fun `downloadAsset should save the asset to the specified output file`() {
        val response = mock(ClassicHttpResponse::class.java)
        val contentStream = "dummy content".byteInputStream()
        val entity = mock(HttpEntity::class.java)

        `when`(entity.content).thenReturn(contentStream)
        `when`(response.entity).thenReturn(entity)
        `when`(httpClient.execute(any(HttpGet::class.java), any(HttpClientResponseHandler::class.java)))
            .thenAnswer { invocation ->
                val handler = invocation.getArgument<HttpClientResponseHandler<String>>(1)
                handler.handleResponse(response)
            }

        val outputFile = "path/to/output.file"
        downloadAssetMojo.outputFile = outputFile

        downloadAssetMojo.downloadAsset(httpClient, "https://api.github.com/repos/dummy_owner/dummy_repo/releases/assets/123")

        val file = File(outputFile)
        assertTrue(file.exists() && file.length() > 0)
        file.delete()
    }

}
