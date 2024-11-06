![alt text](https://bembelnaut.de/wp-content/uploads/2024/11/Maven-Plugin2-768x768.webp)

# About this plugin
This Maven plugin loads an asset file from your release and saves it in the directory you specify.

The plugin was developed with the aim of downloading an OpenApi file from a backend server release. This is then used by another plugin to generate the model classes.

If you like the plugin and you can use it, feel free to include it in your project!

# Usage
You can include this plugin simply:

```
<plugin>
   <groupId>de.bembelnaut</groupId>
   <artifactId>download-github-assets-plugin</artifactId>
   <version>1.0.6</version>
   <executions>
      <goals>
         <goal>download-github-asset</goal>
      </goals>
   </executions>
   <phase>generate-sources</phase>
   <configuration>
      <repoOwner>github-username</repoOwner>
      <repoName>repository-name</repoName>
      <assetName>openapi.json</assetName>
      <outputFile>${project.basedir}/src/main/resources/openapi.json</outputFile>
      <version>v0.2.2</version>
      <githubToken>${env.GITHUB_TOKEN}</githubToken>
   </configuration>
</plugin>
```

**Note**: Make sure you set the githubToken as an environment variable (GITHUB_TOKEN) to access the GitHub release.

Please also note that the token is given the “content: read” permission to the repository.

# How to use

Run plugin:
To run the plugin and download the asset file, you can enter the following command in the terminal:

```
mvn generate-sources
```

# Example: Use in the project

In the following, an OpenApi file is downloaded and then the code generation is triggered. For example:

```
<project>
   ...
   <build>
      ...
      <plugins>
         <plugin>
            <groupId>de.bembelnaut</groupId>
            <artifactId>download-github-assets-plugin</artifactId>
            <version>1.0.6</version>
            <executions>
               <execution>
                  <goals>
                     <goal>download-github-asset</goal>
                  </goals>
                  <configuration>
                     <githubToken>${github-pat}</githubToken>
                     <repoOwner>bembelkosmos</repoOwner>
                     <repoName>deepl-resource-server</repoName>
                     <assetName>deepl-rs-openapi.json</assetName>
                     <version>v0.2.2</version>
                     <outputFile>${project.basedir}/src/main/resources/deepl-rs-openapi.json</outputFile>
                  </configuration>
               </execution>
            </executions>
         </plugin>

         <plugin>
            <groupId>org.openapitools</groupId>
            <artifactId>openapi-generator-maven-plugin</artifactId>
            <version>7.9.0</version>
            <executions>
               <execution>
                  <goals>
                     <goal>generate</goal>
                  </goals>
                  <configuration>
                     <inputSpec>${project.basedir}/src/main/resources/deepl-rs-openapi.json</inputSpec>
                     <generatorName>kotlin</generatorName>
                     <output>${project.build.directory}/generated-sources</output>
                     <apiPackage>de.bembelnaut.translator.api</apiPackage>
                     <modelPackage>de.bembelnaut.translator.model</modelPackage>
                     <configOptions>
                        <interfaceOnly>true</interfaceOnly>
                        <models>true</models>
                        <apis>false</apis>
                        <supportingFiles>false</supportingFiles>
                        <library>jvm-spring-restclient</library>
                        <useSpringBoot3>true</useSpringBoot3>
                        <serializationLibrary>jackson</serializationLibrary>
                     </configOptions>
                  </configuration>
               </execution>   
            </executions>  
         </plugin>
      </plugins>
   </build>
</project>
```

# Configuration

Here are some configuration options:
- **repoOwner**: The GitHub username or the organization where the repository is located.
- **repoName**: The name of the repository from which the file is downloaded.
- **assetName**: The name of the file in the release asset.
- **outputFile**: The path where the file is saved.
- **githubToken**: The GitHub access token for accessing private repositories.
- **version**: The version tag of the release.

# Important notes to internal developers
 
## CI/CD
CI/CD workflow in short:

1. Do not commit on main branch! Create feature or bugfix branch.
2. Crete a changeset to describe mayor, minor or patches. A changeset bot will remind you.
3. After the pull request is created tests and quality checks will run.
4. Merge only if checks are successful!
5. The merge to main will create release pull request. It merges all changesets and creates a new sem-version, and it creates the artifacts (tbd).
6. Publishing the pull request creates new artifacts (tbd) and push it into registries. In addition, it could trigger an deployment to the server.   

### Prerequisites 
- Install node.js
- Configure npm changeset

### Install node.js
Download installer of https://nodejs.org/en/download/package-manager

### Initial configuration of changesets

1. Init changeset: npm install @changesets/cli && npx changeset init
2. Update ./changeset/config.json:
   - Add "privatePackages" to { version: true, tag: true }
   - Verify in ./package.json must be the attributes "name", "private" (true) and "version"
   - see: https://github.com/changesets/changesets/blob/main/docs/versioning-apps.md
3. Use changeset: "npx changeset" to add new changeset
4. Create a PAT:
   - PAT must be permissions to content and pull requests (read and write)
   - Store PAT in github secrets with name CHANGESETS_TOKEN
