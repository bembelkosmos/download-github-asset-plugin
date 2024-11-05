package de.bembelnaut.github.assets

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths

@Mojo(name = "download-github-asset", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
class DownloadAssetMojo : AbstractMojo() {

    @Parameter(property = "githubToken", required = true)
    internal lateinit var githubToken: String

    @Parameter(property = "repoOwner", required = true)
    internal lateinit var repoOwner: String

    @Parameter(property = "repoName", required = true)
    internal lateinit var repoName: String

    @Parameter(property = "version", required = true)
    internal lateinit var version: String

    @Parameter(property = "assetName", required = true)
    internal lateinit var assetName: String

    @Parameter(property = "outputFile", required = true)
    internal lateinit var outputFile: String

    private val objectMapper = ObjectMapper()

    override fun execute() {
        try {
            HttpClients.createDefault().use { client ->
                val assetsUrl = fetchAssetsUrlOfRelease(client) ?: throw MojoExecutionException("Release $version not found.")
                val assetUrl = fetchAssetUrl(client, assetsUrl) ?: throw MojoExecutionException("Asset $assetName not found.")

                downloadAsset(client, assetUrl)
                log.info("Downloaded $assetName to $outputFile")
            }
        } catch (e: Exception) {
            throw MojoExecutionException("Error downloading asset", e)
        }
    }

    internal fun fetchAssetsUrlOfRelease(client: CloseableHttpClient): String? {
        val releaseInfoUrl = "https://api.github.com/repos/$repoOwner/$repoName/releases/tags/$version"
        log.info("Request info of $releaseInfoUrl")

        val request = HttpGet(releaseInfoUrl).apply {
            setHeader(HttpHeaders.AUTHORIZATION, "Bearer $githubToken")
            setHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
        }

        client.execute(request).use { response ->
            val releaseInfoNode = objectMapper.readTree(response.entity.content)
            val assertsUrl = releaseInfoNode["assets_url"]

            log.debug("Response of info request: $releaseInfoNode")
            log.info("Response of info request: $assertsUrl")

            return assertsUrl?.asText()
        }
    }

    internal fun fetchAssetUrl(client: CloseableHttpClient, assetsUrl: String): String? {
        log.info("Request assets of $assetsUrl")

        val request = HttpGet(assetsUrl).apply {
            setHeader(HttpHeaders.AUTHORIZATION, "Bearer $githubToken")
            setHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
        }

        client.execute(request).use { response ->
            val releaseNode = objectMapper.readTree(response.entity.content)
            log.info("Response of assets request: $releaseNode")

            return releaseNode?.find { it["name"].asText() == assetName }?.get("url")?.asText()
        }
    }

    internal fun downloadAsset(client: CloseableHttpClient, assetUrl: String) {
        log.info("Download asset $assetUrl")

        val request = HttpGet(assetUrl).apply {
            setHeader(HttpHeaders.AUTHORIZATION, "Bearer $githubToken")
            setHeader(HttpHeaders.ACCEPT, "application/octet-stream")
        }

        client.execute(request).use { response ->
            log.info("Response of download request: ${response.statusLine}")

            Files.createDirectories(Paths.get(outputFile).parent)
            FileOutputStream(File(outputFile)).use { outputStream ->
                response.entity.content.copyTo(outputStream)
            }
        }
    }
}
