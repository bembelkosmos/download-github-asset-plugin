package de.bembelnaut.github.assets

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.io.File

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
        val response = mock(CloseableHttpResponse::class.java)
        val entity = StringEntity("""{"assets_url": "https://api.github.com/repos/dummy_owner/dummy_repo/releases/assets"}""")
        `when`(response.entity).thenReturn(entity)
        `when`(httpClient.execute(any(HttpGet::class.java))).thenReturn(response)

        val result = downloadAssetMojo.fetchAssetsUrlOfRelease(httpClient)
        assertEquals("https://api.github.com/repos/dummy_owner/dummy_repo/releases/assets", result)
    }


    @Test
    fun `fetchAssetUrl should return asset URL when asset is found`() {
        val response = mock(CloseableHttpResponse::class.java)
        val entity = StringEntity("""[{"name": "test-asset", "url": "https://api.github.com/repos/dummy_owner/dummy_repo/releases/assets/123"}]""")
        `when`(response.entity).thenReturn(entity)
        `when`(httpClient.execute(any(HttpGet::class.java))).thenReturn(response)

        downloadAssetMojo.assetName = "test-asset"

        val result = downloadAssetMojo.fetchAssetUrl(httpClient, "https://api.github.com/repos/dummy_owner/dummy_repo/releases/assets")
        assertEquals("https://api.github.com/repos/dummy_owner/dummy_repo/releases/assets/123", result)
    }


    @Test
    fun `downloadAsset should save the asset to the specified output file`() {
        val response = mock(CloseableHttpResponse::class.java)
        val contentStream = "dummy content".byteInputStream()
        val entity = mock(HttpEntity::class.java)

        `when`(entity.content).thenReturn(contentStream)
        `when`(response.entity).thenReturn(entity)
        `when`(httpClient.execute(any(HttpGet::class.java))).thenReturn(response)

        val outputFile = "path/to/output.file"
        downloadAssetMojo.outputFile = outputFile

        downloadAssetMojo.downloadAsset(httpClient, "https://api.github.com/repos/dummy_owner/dummy_repo/releases/assets/123")

        val file = File(outputFile)
        assertTrue(file.exists() && file.length() > 0)
        file.delete()  // Bereinigen des Testdateisystems
    }

}
