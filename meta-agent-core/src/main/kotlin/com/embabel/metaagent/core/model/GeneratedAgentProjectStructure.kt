package com.embabel.metaagent.core.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.nio.file.Path
import java.time.Instant

/**
 * Complete structure representing a generated agent project.
 * 
 * This is the root data class that contains all information needed to create
 * a fully functional agent project, including source files, tests, configuration,
 * build files, documentation, and metadata.
 * 
 * @param projectName Human-readable name of the generated project
 * @param rootPath Absolute path where the project will be created
 * @param sourceFiles List of generated source code files (required, at least one)
 * @param testFiles List of generated test files for validating the agent
 * @param configurationFiles List of configuration files (properties, YAML, etc.)
 * @param buildFiles List of build system files (Maven POM, Gradle scripts)
 * @param documentationFiles List of generated documentation files
 * @param environmentFiles List of environment setup files (Docker, scripts)
 * @param projectMetadata Project information and metadata
 * @param buildInformation Results from compilation, testing, and validation
 */
data class GeneratedAgentProjectStructure(
    @field:NotBlank(message = "Project name cannot be blank")
    val projectName: String,
    
    @field:NotBlank(message = "Root path cannot be blank")
    val rootPath: Path,
    
    @field:NotEmpty(message = "At least one source file must be generated")
    val sourceFiles: List<GeneratedSourceFile>,
    
    val testFiles: List<GeneratedTestFile> = emptyList(),
    
    val configurationFiles: List<GeneratedConfigFile> = emptyList(),
    
    val buildFiles: List<GeneratedBuildFile> = emptyList(),
    
    val documentationFiles: List<GeneratedDocFile> = emptyList(),
    
    val environmentFiles: List<GeneratedEnvFile> = emptyList(),
    
    val projectMetadata: ProjectMetadata,
    
    val buildInformation: BuildInformation
)

/**
 * Represents a generated source code file.
 * 
 * Contains the complete information needed to create a source file in the
 * generated agent project, including content, metadata, and classification.
 * 
 * @param fileName Name of the source file (e.g., "UserAgent.kt")
 * @param relativePath Path relative to project root (e.g., "src/main/kotlin/com/example/UserAgent.kt")
 * @param content Complete source code content of the file
 * @param fileType Classification of the source file type (agent, entity, service, etc.)
 * @param language Programming language of the source file
 * @param generatedAt Timestamp when the file was generated
 */
data class GeneratedSourceFile(
    @field:NotBlank(message = "File name cannot be blank")
    val fileName: String,
    
    @field:NotBlank(message = "Relative path cannot be blank")
    val relativePath: String,
    
    @field:NotBlank(message = "Content cannot be blank")
    val content: String,
    
    val fileType: SourceFileType,
    
    val language: TargetLanguage,
    
    val generatedAt: Instant = Instant.now()
)

/**
 * Represents a generated test file.
 * 
 * Contains test code that validates the behavior of generated agent components.
 * Links to the source file being tested for traceability.
 * 
 * @param fileName Name of the test file (e.g., "UserAgentTest.kt")
 * @param relativePath Path relative to project root (e.g., "src/test/kotlin/com/example/UserAgentTest.kt")
 * @param content Complete test code content
 * @param testType Classification of test type (unit, integration, etc.)
 * @param targetSourceFile Name of the source file this test covers
 * @param generatedAt Timestamp when the test file was generated
 */
data class GeneratedTestFile(
    val fileName: String,
    val relativePath: String,
    val content: String,
    val testType: TestType,
    val targetSourceFile: String,
    val generatedAt: Instant = Instant.now()
)

/**
 * Represents a generated configuration file.
 * 
 * Contains application configuration, properties, or other settings
 * needed for the generated agent to function properly.
 * 
 * @param fileName Name of the configuration file (e.g., "application.yml")
 * @param relativePath Path relative to project root (e.g., "src/main/resources/application.yml")
 * @param content Configuration content in appropriate format
 * @param configType Classification of configuration type (application, logging, etc.)
 * @param generatedAt Timestamp when the configuration file was generated
 */
data class GeneratedConfigFile(
    val fileName: String,
    val relativePath: String,
    val content: String,
    val configType: ConfigType,
    val generatedAt: Instant = Instant.now()
)

/**
 * Represents a generated build configuration file.
 * 
 * Contains build system configuration (Maven POM, Gradle build script) 
 * with dependencies and build settings for the generated agent project.
 * 
 * @param fileName Name of the build file (e.g., "pom.xml", "build.gradle.kts")
 * @param relativePath Path relative to project root (typically just the filename)
 * @param content Build configuration content with dependencies and plugins
 * @param buildTool Build system this file is configured for
 * @param generatedAt Timestamp when the build file was generated
 */
data class GeneratedBuildFile(
    val fileName: String,
    val relativePath: String,
    val content: String,
    val buildTool: BuildTool,
    val generatedAt: Instant = Instant.now()
)

/**
 * Represents a generated documentation file.
 * 
 * @param fileName Name of the documentation file
 * @param relativePath Path relative to project root
 * @param content Generated documentation content
 * @param docType Type of documentation (README, API_DOCS, etc.)
 * @param generatedAt Timestamp when the file was generated
 */
data class GeneratedDocFile(
    val fileName: String,
    val relativePath: String,
    val content: String,
    val docType: DocumentationFileType,
    val generatedAt: Instant = Instant.now()
)

/**
 * Represents a generated environment setup file.
 * 
 * Contains environment configuration files such as Docker files,
 * deployment scripts, or environment variable templates needed
 * to run the generated agent project.
 * 
 * @param fileName Name of the environment file (e.g., "Dockerfile", "docker-compose.yml")
 * @param relativePath Path relative to project root
 * @param content Environment configuration content
 * @param envType Type of environment file (Docker, script, etc.)
 * @param generatedAt Timestamp when the environment file was generated
 */
data class GeneratedEnvFile(
    val fileName: String,
    val relativePath: String,
    val content: String,
    val envType: EnvironmentType,
    val generatedAt: Instant = Instant.now()
)

/**
 * Metadata information for the generated project.
 * 
 * Contains Maven/Gradle project coordinates, versioning information,
 * and dependency information for the generated agent project.
 * 
 * @param groupId Maven group ID (e.g., "com.example")
 * @param artifactId Maven artifact ID (project name for Maven coordinates)
 * @param version Project version following semantic versioning
 * @param description Human-readable project description
 * @param createdAt Timestamp when the project was created
 * @param generatedBy Tool or system that generated this project
 * @param embabelAgentVersion Version of the Embabel Agent framework used
 * @param dependencies List of external dependencies required by the project
 */
data class ProjectMetadata(
    val groupId: String,
    val artifactId: String,
    val version: String = "1.0.0-SNAPSHOT",
    val description: String = "",
    val createdAt: Instant = Instant.now(),
    val generatedBy: String = "Meta-Agent",
    val embabelAgentVersion: String,
    val dependencies: List<ProjectDependency> = emptyList()
)

/**
 * Represents an external dependency required by the generated project.
 * 
 * Contains Maven/Gradle dependency coordinates and scope information
 * for libraries and frameworks that the generated agent requires.
 * 
 * @param groupId Maven group ID of the dependency
 * @param artifactId Maven artifact ID of the dependency
 * @param version Version of the dependency to include
 * @param scope Dependency scope (compile, test, runtime, etc.)
 * @param type Type of dependency artifact (JAR, POM, WAR, etc.)
 */
data class ProjectDependency(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val scope: DependencyScope = DependencyScope.COMPILE,
    val type: DependencyType = DependencyType.JAR
)

/**
 * Information about the build process results for the generated project.
 * 
 * Tracks the outcome of compilation, testing, and validation steps
 * performed on the generated agent project to ensure it's functional.
 * 
 * @param compilationResult Results from compiling the generated source code
 * @param testResult Results from running the generated test suite
 * @param validationResult Results from code quality and validation checks
 * @param buildTimestamp When the build process was executed
 */
data class BuildInformation(
    val compilationResult: CompilationResult? = null,
    val testResult: TestResult? = null,
    val validationResult: ValidationResult? = null,
    val buildTimestamp: Instant? = null
)

/**
 * Results from compiling the generated source code.
 * 
 * Contains information about the compilation process including
 * success status, errors, warnings, and list of compiled files.
 * 
 * @param success Whether the compilation completed successfully
 * @param errors List of compilation errors that prevented successful build
 * @param warnings List of compilation warnings (non-fatal issues)
 * @param compiledFiles List of file paths that were successfully compiled
 */
data class CompilationResult(
    val success: Boolean,
    val errors: List<CompilationError> = emptyList(),
    val warnings: List<CompilationWarning> = emptyList(),
    val compiledFiles: List<String> = emptyList()
)

/**
 * Represents a compilation error encountered during the build process.
 * 
 * Contains detailed location and description information for debugging
 * compilation issues in the generated source code.
 * 
 * @param file Path to the file where the error occurred
 * @param line Line number where the error was found (1-based)
 * @param column Column number where the error was found (1-based)
 * @param message Descriptive error message from the compiler
 * @param severity Severity level of the compilation error
 */
data class CompilationError(
    val file: String,
    val line: Int,
    val column: Int,
    val message: String,
    val severity: ErrorSeverity
)

/**
 * Represents a compilation warning encountered during the build process.
 * 
 * Contains detailed location and description information for non-fatal
 * compilation issues that don't prevent successful build.
 * 
 * @param file Path to the file where the warning occurred
 * @param line Line number where the warning was found (1-based)
 * @param column Column number where the warning was found (1-based)
 * @param message Descriptive warning message from the compiler
 */
data class CompilationWarning(
    val file: String,
    val line: Int,
    val column: Int,
    val message: String
)

/**
 * Results from running the generated test suite.
 * 
 * Contains comprehensive information about test execution including
 * pass/fail counts and paths to detailed test reports.
 * 
 * @param success Whether all tests passed successfully
 * @param totalTests Total number of tests that were discovered and attempted
 * @param passedTests Number of tests that passed successfully
 * @param failedTests Number of tests that failed
 * @param skippedTests Number of tests that were skipped during execution
 * @param testReports List of paths to detailed test report files
 */
data class TestResult(
    val success: Boolean,
    val totalTests: Int,
    val passedTests: Int,
    val failedTests: Int,
    val skippedTests: Int,
    val testReports: List<String> = emptyList()
)

/**
 * Results from code quality and validation checks.
 * 
 * Contains information about static analysis, code quality metrics,
 * and validation rule compliance for the generated project.
 * 
 * @param success Whether all validation checks passed
 * @param validationErrors List of validation rule violations found
 * @param codeQualityScore Optional numeric score representing overall code quality (0.0-1.0)
 */
data class ValidationResult(
    val success: Boolean,
    val validationErrors: List<ValidationError> = emptyList(),
    val codeQualityScore: Double? = null
)

/**
 * Represents a validation rule violation found during code quality checks.
 * 
 * Contains detailed information about code quality issues, style violations,
 * or other validation rule failures in the generated code.
 * 
 * @param file Path to the file where the validation error occurred
 * @param line Line number where the validation error was found (1-based)
 * @param rule Name or identifier of the validation rule that was violated
 * @param message Descriptive message explaining the validation failure
 * @param severity Severity level of the validation error
 */
data class ValidationError(
    val file: String,
    val line: Int,
    val rule: String,
    val message: String,
    val severity: ErrorSeverity
)

/**
 * Classifications for different types of generated source files.
 * 
 * Distinguishes between different architectural components and their roles
 * within the generated agent project structure.
 */
enum class SourceFileType {
    /** Main agent class containing agent logic and behavior */
    AGENT_CLASS,
    
    /** Domain model entities representing business objects */
    DOMAIN_ENTITY,
    
    /** Service classes containing business logic and operations */
    SERVICE_CLASS,
    
    /** Configuration classes for application setup and settings */
    CONFIGURATION_CLASS,
    
    /** Utility classes providing common helper functions */
    UTILITY_CLASS,
    
    /** Aspect-oriented programming classes for audit functionality */
    AUDIT_ASPECT,
    
    /** Configuration classes specifically for logging setup */
    LOGGING_CONFIG
}

/**
 * Classifications for different types of generated test files.
 * 
 * Distinguishes between various testing approaches and scopes
 * for validating different aspects of the generated agent.
 */
enum class TestType {
    /** Tests for individual classes or methods in isolation */
    UNIT_TEST,
    
    /** Tests for component interaction and system integration */
    INTEGRATION_TEST,
    
    /** Tests for API endpoints and external interface behavior */
    API_TEST,
    
    /** Tests specifically for agent behavior and decision-making logic */
    AGENT_BEHAVIOR_TEST
}

/**
 * Classifications for different types of configuration files.
 * 
 * Distinguishes between various configuration formats and purposes
 * within the generated agent project.
 */
enum class ConfigType {
    /** Standard Java properties file format for application configuration */
    APPLICATION_PROPERTIES,
    
    /** YAML format configuration file for application settings */
    APPLICATION_YML,
    
    /** Logback framework configuration for logging behavior */
    LOGBACK_CONFIG,
    
    /** Configuration specific to audit and compliance features */
    AUDIT_CONFIG,
    
    /** Spring Framework specific configuration files */
    SPRING_CONFIG
}

/**
 * Types of documentation files that can be generated.
 * 
 * Distinguishes between different kinds of documentation content,
 * separate from the format preference (markdown, asciidoc, etc.).
 */
enum class DocumentationFileType {
    README,
    API_DOCS,
    USER_GUIDE,
    SETUP_INSTRUCTIONS,
    CHANGELOG
}

/**
 * Classifications for different types of environment setup files.
 * 
 * Distinguishes between various deployment and environment configuration
 * approaches for running the generated agent project.
 */
enum class EnvironmentType {
    /** Docker Compose YAML file for multi-container deployment */
    DOCKER_COMPOSE,
    
    /** Dockerfile for building container images */
    DOCKERFILE,
    
    /** Bash shell script for Unix/Linux environment setup */
    BASH_SCRIPT,
    
    /** PowerShell script for Windows environment setup */
    POWERSHELL_SCRIPT,
    
    /** Template file for environment variable configuration */
    ENV_TEMPLATE
}

/**
 * Maven/Gradle dependency scopes defining when dependencies are available.
 * 
 * Specifies the classpath visibility and usage context for project dependencies
 * following standard Maven dependency scope conventions.
 */
enum class DependencyScope {
    /** Available in all phases - compilation, testing, and runtime */
    COMPILE,
    
    /** Available for compilation and testing, but provided by runtime environment */
    PROVIDED,
    
    /** Not needed for compilation, but required at runtime */
    RUNTIME,
    
    /** Only available during test compilation and execution */
    TEST,
    
    /** Available from local system, not searched in repositories */
    SYSTEM,
    
    /** Used for importing dependency management from other POMs */
    IMPORT
}

/**
 * Types of Maven/Gradle dependency artifacts.
 * 
 * Specifies the packaging format and type of dependency artifacts
 * that can be included in the generated project.
 */
enum class DependencyType {
    /** Standard Java Archive file containing compiled classes */
    JAR,
    
    /** Project Object Model file for dependency management */
    POM,
    
    /** Web Application Archive for web applications */
    WAR,
    
    /** Enterprise Application Archive for enterprise applications */
    EAR
}

/**
 * Severity levels for errors, warnings, and informational messages.
 * 
 * Used to classify the importance and impact of compilation errors,
 * validation issues, and other diagnostic messages.
 */
enum class ErrorSeverity {
    /** Critical issues that prevent successful compilation or execution */
    ERROR,
    
    /** Non-critical issues that should be addressed but don't prevent functionality */
    WARNING,
    
    /** Informational messages providing additional context or suggestions */
    INFO
}