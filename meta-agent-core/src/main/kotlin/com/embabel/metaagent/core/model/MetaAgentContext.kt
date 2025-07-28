package com.embabel.metaagent.core.model


import com.embabel.agent.domain.io.UserInput
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import java.util.*

/**
 * Context for meta-agent generation process.
 * 
 * This data class encapsulates all the information needed to generate an agent,
 * including user requirements, target language preferences, and generation settings.
 * It serves as the primary input to the agent generation pipeline.
 * 
 * The context integrates with embabel-agent-api's UserInput structure while adding
 * meta-agent specific configuration for code generation, build tools, and frameworks.
 * 
 * @param id Unique identifier for this generation context
 * @param userInput User requirements from embabel-agent-api containing frameworks, metadata, and timestamp
 * @param targetLanguage Target programming language for generated agent code
 * @param sessionId Unique identifier for the current generation session
 * @param generationSettings Configuration settings controlling code generation behavior
 */
data class MetaAgentContext(
    val id: UUID = UUID.randomUUID(),

    @field:Valid
    val userInput: UserInput, // Contains preferredFrameworks, contextMetadata, timestamp

    @field:NotEmpty(message = "Target language must be specified")
    val targetLanguage: TargetLanguage,

    val sessionId: UUID = UUID.randomUUID(),

    val generationSettings: GenerationSettings = GenerationSettings()
)

/**
 * Supported target programming languages for agent generation.
 * 
 * Defines the JVM languages that the meta-agent can generate code for,
 * including language-specific configuration like file extensions and
 * package organization conventions.
 * 
 * @param displayName Human-readable name of the programming language
 * @param fileExtension File extension used for source files in this language
 * @param packagePrefix Package organization prefix for generated code
 */
enum class TargetLanguage(
    val displayName: String,
    val fileExtension: String,
    val packagePrefix: String
) {
    /** Kotlin programming language with modern JVM features */
    KOTLIN("Kotlin", ".kt", "kotlin"),
    
    /** Java programming language with enterprise ecosystem */
    JAVA("Java", ".java", "java"),
    
    /** Scala programming language with functional programming support */
    SCALA("Scala", ".scala", "scala");
    
    companion object {
        /**
         * Parses target language from string representation.
         * 
         * Supports both full language names and common abbreviations.
         * Case-insensitive matching for user convenience.
         * 
         * @param language String representation of the language
         * @return TargetLanguage enum value
         * @throws IllegalArgumentException if language is not supported
         */
        fun fromString(language: String): TargetLanguage = when (language.lowercase()) {
            "kotlin", "kt" -> KOTLIN
            "java" -> JAVA
            "scala" -> SCALA
            else -> throw IllegalArgumentException("Unsupported language: $language. Supported: kotlin, java, scala")
        }
    }
}

/**
 * Configuration settings for agent code generation.
 * 
 * Controls various aspects of the generated agent project including testing,
 * documentation, build tools, and framework versions. These settings influence
 * the structure and content of the generated code.
 * 
 * @param includeTests Whether to generate unit and integration tests
 * @param includeDocumentation Whether to generate comprehensive documentation
 * @param testFramework Testing framework to use for generated tests
 * @param documentationType Format for generated documentation (markdown, asciidoc, etc.)
 * @param buildTool Build system to use (Maven or Gradle)
 * @param springBootVersion Version of Spring Boot framework to target
 * @param kotlinVersion Version of Kotlin language to target (when applicable)
 * @param javaVersion Target Java version for compilation and runtime
 */
data class GenerationSettings(
    val includeTests: Boolean = true,
    val includeDocumentation: Boolean = true,
    val testFramework: TestFramework = TestFramework.JUNIT5,
    val documentationType: DocumentationType = DocumentationType.MARKDOWN,
    // enableAuditFramework removed for MVP - will be added in Sprint 5
    val buildTool: BuildTool = BuildTool.MAVEN,
    val springBootVersion: SpringBootVersion = SpringBootVersion.V3_2_0,
    val kotlinVersion: KotlinVersion = KotlinVersion.V2_1_0,
    val javaVersion: JavaVersion = JavaVersion.V21
)

/**
 * Supported build tools for generated agent projects.
 * 
 * Defines the build systems that can be used to compile and package
 * the generated agent code, including their configuration files and
 * standard conventions.
 * 
 * @param displayName Human-readable name of the build tool
 * @param configFile Primary configuration file name for this build tool
 */
enum class BuildTool(val displayName: String, val configFile: String) {
    /** Apache Maven build system with XML-based configuration */
    MAVEN("Apache Maven", "pom.xml"),
    
    /** Gradle build system with Kotlin DSL configuration */
    GRADLE("Gradle", "build.gradle.kts");
    
    companion object {
        /**
         * Parses build tool from string representation.
         * 
         * Supports both full tool names and common abbreviations.
         * Case-insensitive matching for user convenience.
         * 
         * @param tool String representation of the build tool
         * @return BuildTool enum value
         * @throws IllegalArgumentException if build tool is not supported
         */
        fun fromString(tool: String): BuildTool = when (tool.lowercase()) {
            "maven", "mvn" -> MAVEN
            "gradle" -> GRADLE
            else -> throw IllegalArgumentException("Unsupported build tool: $tool. Supported: maven, gradle")
        }
    }
}

/**
 * Supported testing frameworks for generated agent projects.
 * 
 * Defines the testing libraries that can be used for unit and integration
 * testing of generated agents, with language-specific recommendations.
 * 
 * @param displayName Human-readable name of the testing framework
 * @param dependency Maven/Gradle dependency coordinate for the framework
 */
enum class TestFramework(val displayName: String, val dependency: String) {
    /** JUnit 5 - Modern Java testing framework with advanced features */
    JUNIT5("JUnit 5", "org.junit.jupiter:junit-jupiter"),
    
    /** JUnit 4 - Legacy Java testing framework for compatibility */
    JUNIT4("JUnit 4", "junit:junit"),
    
    /** TestNG - Alternative Java testing framework with advanced configuration */
    TESTNG("TestNG", "org.testng:testng"),
    
    /** Spock - Groovy-based testing framework with expressive syntax */
    SPOCK("Spock", "org.spockframework:spock-core"),
    
    /** Kotest - Kotlin-native testing framework with multiplatform support */
    KOTEST("Kotest", "io.kotest:kotest-runner-junit5");
    
    companion object {
        /**
         * Recommends appropriate testing framework for target language.
         * 
         * Provides language-specific defaults based on ecosystem conventions
         * and community best practices.
         * 
         * @param language Target programming language
         * @return Recommended TestFramework for the language
         */
        fun forLanguage(language: TargetLanguage): TestFramework = when (language) {
            TargetLanguage.KOTLIN -> KOTEST
            TargetLanguage.JAVA -> JUNIT5
            TargetLanguage.SCALA -> JUNIT5
        }
    }
}

/**
 * Supported documentation formats for generated agent projects.
 * 
 * Defines the markup languages and formats that can be used for
 * generating comprehensive documentation alongside the agent code.
 * 
 * @param displayName Human-readable name of the documentation format
 * @param fileExtension File extension used for documents in this format
 */
enum class DocumentationType(val displayName: String, val fileExtension: String) {
    /** Markdown - Lightweight markup language with wide tool support */
    MARKDOWN("Markdown", ".md"),
    
    /** AsciiDoc - Advanced markup language with rich formatting capabilities */
    ASCIIDOC("AsciiDoc", ".adoc"),
    
    /** HTML - Standard web markup language for interactive documentation */
    HTML("HTML", ".html");
    
    companion object {
        /**
         * Parses documentation type from string representation.
         * 
         * Supports both full format names and common file extensions.
         * Case-insensitive matching for user convenience.
         * 
         * @param type String representation of the documentation format
         * @return DocumentationType enum value
         * @throws IllegalArgumentException if format is not supported
         */
        fun fromString(type: String): DocumentationType = when (type.lowercase()) {
            "markdown", "md" -> MARKDOWN
            "asciidoc", "adoc" -> ASCIIDOC
            "html" -> HTML
            else -> throw IllegalArgumentException("Unsupported documentation type: $type. Supported: markdown, asciidoc, html")
        }
    }
}

/**
 * Supported Spring Boot framework versions for generated agents.
 * 
 * Defines the Spring Boot versions that are compatible with the meta-agent
 * framework and embabel-agent-api integration. Focuses on Spring Boot 3.x
 * series for modern Java/Kotlin ecosystem support.
 * 
 * @param version Semantic version string of Spring Boot
 * @param displayName Human-readable version description
 */
enum class SpringBootVersion(val version: String, val displayName: String) {
    /** Spring Boot 3.2.0 - Latest stable with enhanced performance */
    V3_2_0("3.2.0", "Spring Boot 3.2.0"),
    
    /** Spring Boot 3.1.0 - Stable release with Jakarta EE support */
    V3_1_0("3.1.0", "Spring Boot 3.1.0"),
    
    /** Spring Boot 3.0.0 - First Jakarta EE compatible release */
    V3_0_0("3.0.0", "Spring Boot 3.0.0");
    
    companion object {
        /**
         * Parses Spring Boot version from string representation.
         * 
         * Matches exact version strings to ensure compatibility with
         * the embabel-agent-api and meta-agent framework.
         * 
         * @param version String representation of Spring Boot version
         * @return SpringBootVersion enum value
         * @throws IllegalArgumentException if version is not supported
         */
        fun fromString(version: String): SpringBootVersion = when (version) {
            "3.2.0" -> V3_2_0
            "3.1.0" -> V3_1_0  
            "3.0.0" -> V3_0_0
            else -> throw IllegalArgumentException("Unsupported Spring Boot version: $version. Supported: 3.2.0, 3.1.0, 3.0.0")
        }
    }
}

/**
 * Supported Kotlin language versions for generated agents.
 * 
 * Defines the Kotlin compiler versions that are compatible with the
 * meta-agent framework and provide the language features needed for
 * generated agent code.
 * 
 * @param version Semantic version string of Kotlin
 * @param displayName Human-readable version description
 */
enum class KotlinVersion(val version: String, val displayName: String) {
    /** Kotlin 2.1.0 - Latest stable with K2 compiler and performance improvements */
    V2_1_0("2.1.0", "Kotlin 2.1.0"),
    
    /** Kotlin 2.0.0 - Major release with K2 compiler stabilization */
    V2_0_0("2.0.0", "Kotlin 2.0.0"),
    
    /** Kotlin 1.9.0 - Stable release with multiplatform improvements */
    V1_9_0("1.9.0", "Kotlin 1.9.0");
    
    companion object {
        /**
         * Parses Kotlin version from string representation.
         * 
         * Matches exact version strings to ensure compatibility with
         * Spring Boot and embabel-agent-api dependencies.
         * 
         * @param version String representation of Kotlin version
         * @return KotlinVersion enum value
         * @throws IllegalArgumentException if version is not supported
         */
        fun fromString(version: String): KotlinVersion = when (version) {
            "2.1.0" -> V2_1_0
            "2.0.0" -> V2_0_0
            "1.9.0" -> V1_9_0
            else -> throw IllegalArgumentException("Unsupported Kotlin version: $version. Supported: 2.1.0, 2.0.0, 1.9.0")
        }
    }
}

/**
 * Supported Java runtime versions for generated agents.
 * 
 * Defines the Java Virtual Machine versions that are compatible with
 * the meta-agent framework, Spring Boot 3.x, and embabel-agent-api.
 * Focuses on LTS (Long Term Support) releases for stability.
 * 
 * @param version Java version number as string
 * @param displayName Human-readable version description with LTS indicator
 */
enum class JavaVersion(val version: String, val displayName: String) {
    /** Java 21 LTS - Latest long-term support with virtual threads and pattern matching */
    V21("21", "Java 21 LTS"),
    
    /** Java 17 LTS - Stable long-term support with sealed classes and records */
    V17("17", "Java 17 LTS"),
    
    /** Java 11 LTS - Minimum supported version for Spring Boot 3.x compatibility */
    V11("11", "Java 11 LTS");
    
    companion object {
        /**
         * Parses Java version from string representation.
         * 
         * Supports standard Java version numbers used in Maven and Gradle
         * configuration. Only LTS versions are supported for stability.
         * 
         * @param version String representation of Java version
         * @return JavaVersion enum value
         * @throws IllegalArgumentException if version is not supported
         */
        fun fromString(version: String): JavaVersion = when (version) {
            "21" -> V21
            "17" -> V17
            "11" -> V11
            else -> throw IllegalArgumentException("Unsupported Java version: $version. Supported: 21, 17, 11")
        }
    }
}