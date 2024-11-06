package de.bembelnaut.github.assets

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.HttpHeaders
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Mojo class to download an asset of a GitHub release.
 *
 * The download are divided into four parts:
 * - fetching the release info to get the assets url
 * - fetching assets info to get the desired asset
 * - download and save asset
 *
 */
@Mojo(name = "download-github-asset", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
class DownloadAssetMojo : AbstractMojo() {

    /**
     * The GitHub access token for accessing private repositories.
     */
    @Parameter(property = "githubToken", required = true)
    internal lateinit var githubToken: String

    /**
     * The GitHub username or the organization where the repository is located.
     */
    @Parameter(property = "repoOwner", required = true)
    internal lateinit var repoOwner: String

    /**
     * The name of the repository from which the file is downloaded.
     */
    @Parameter(property = "repoName", required = true)
    internal lateinit var repoName: String

    /**
     * The version tag of the release. NOT "latest"!
     */
    @Parameter(property = "version", required = true)
    internal lateinit var version: String

    /**
     * The name of the file in the release asset.
     */
    @Parameter(property = "assetName", required = true)
    internal lateinit var assetName: String

    /**
     * The path where the file is saved.
     */
    @Parameter(property = "outputFile", required = true)
    internal lateinit var outputFile: String

    /*
     * Mapper for converting response to JSON.
     */
    private val objectMapper = ObjectMapper()

    /**
     * Main function of mojo.
     */
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

    /**
     * Fetching the release info to get the assets url
     * @return assets url.
     */
    internal fun fetchAssetsUrlOfRelease(client: CloseableHttpClient): String? {
        val releaseInfoUrl = "https://api.github.com/repos/$repoOwner/$repoName/releases/tags/$version"
        log.info("Request info of $releaseInfoUrl")

        val request = HttpGet(releaseInfoUrl).apply {
            setHeader(HttpHeaders.AUTHORIZATION, "Bearer $githubToken")
            setHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
        }

        return client.execute(request) { response ->
            val releaseInfoNode = objectMapper.readTree(response.entity.content)
            val assertsUrl = releaseInfoNode["assets_url"]

            log.debug("Response of info request: $releaseInfoNode")
            log.info("Response of info request: $assertsUrl")

            assertsUrl?.asText()
        }
    }

    /**
     * Fetching assets info to get the desired asset
     * @return single asset url.
     */
    internal fun fetchAssetUrl(client: CloseableHttpClient, assetsUrl: String): String? {
        log.info("Request assets of $assetsUrl")

        val request = HttpGet(assetsUrl).apply {
            setHeader(HttpHeaders.AUTHORIZATION, "Bearer $githubToken")
            setHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
        }

        return client.execute(request) { response ->
            val releaseNode = objectMapper.readTree(response.entity.content)
            log.info("Response of assets request: $releaseNode")

            releaseNode?.find { it["name"].asText() == assetName }?.get("url")?.asText()
        }
    }

    /**
     * Download and save asset
     */
    internal fun downloadAsset(client: CloseableHttpClient, assetUrl: String) {
        log.info("Download asset $assetUrl")

        val request = HttpGet(assetUrl).apply {
            setHeader(HttpHeaders.AUTHORIZATION, "Bearer $githubToken")
            setHeader(HttpHeaders.ACCEPT, "application/octet-stream")
        }

        client.execute(request) { response ->
            log.info("Response of download request: ${response.code}")

            Files.createDirectories(Paths.get(outputFile).parent)
            FileOutputStream(File(outputFile)).use { outputStream ->
                response.entity.content.copyTo(outputStream)
            }
        }
    }
}
