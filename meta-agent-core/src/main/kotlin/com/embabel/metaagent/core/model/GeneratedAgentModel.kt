package com.embabel.metaagent.core.model

import com.embabel.agent.core.Action
import com.embabel.agent.core.Agent
import com.embabel.agent.core.Condition
import com.embabel.agent.core.Goal
import com.embabel.common.core.types.Semver
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import java.time.Instant
import java.util.*

/**
 * Generated agent model that integrates with embabel-agent-api structures.
 * 
 * This model represents a complete agent ready for code generation, using the proven
 * Agent, Action, Goal, and Condition structures from embabel-agent-api for type safety
 * and GOAP planning integration.
 * 
 * @param id Unique identifier for the generated agent model
 * @param agent Core Agent structure from embabel-agent-api with all components
 * @param packageName Java package name following standard conventions
 * @param discoveredTools External tools integrated during generation
 * @param domainEntities Domain-specific data structures for the agent
 * @param generationMetadata Metadata about the generation process
 * @param generationContext Context used during agent generation
 */
data class GeneratedAgentModel(
    val id: UUID = UUID.randomUUID(),
    
    @field:Valid
    val agent: Agent,
    
    @field:NotBlank(message = "Package name cannot be blank")
    @field:Pattern(regexp = "^[a-z]+(\\.[a-z][a-z0-9]*)*$", message = "Package name must follow Java package naming convention")
    val packageName: String,
    
    val discoveredTools: List<DiscoveredTool> = emptyList(),
    
    val domainEntities: List<DomainEntity> = emptyList(),
    
    val generationMetadata: GenerationMetadata,
    
    @field:Valid
    val generationContext: MetaAgentContext,
    
    @field:NotBlank(message = "Generated code cannot be blank")
    val generatedCode: String = ""
) {
    
    /**
     * Convenience property to access the agent name.
     * 
     * @return The name of the generated agent
     */
    val agentName: String
        get() = agent.name
    
    /**
     * Convenience property to access agent actions.
     * 
     * @return List of actions from the embabel-agent-api Agent structure
     */
    val actions: List<Action>
        get() = agent.actions
    
    /**
     * Convenience property to access agent goals.
     * 
     * @return Set of goals from the embabel-agent-api Agent structure
     */
    val goals: Set<Goal>
        get() = agent.goals
    
    /**
     * Convenience property to access agent conditions.
     * 
     * @return Set of conditions from the embabel-agent-api Agent structure
     */
    val conditions: Set<Condition>
        get() = agent.conditions
}

/**
 * Metadata about the agent generation process.
 * 
 * Contains information about how the agent was generated, when it was created,
 * and any additional context that may be useful for maintenance or debugging.
 * 
 * @param version Version of the generated agent (defaults to 1.0.0)
 * @param author Author of the generated agent (defaults to Meta-Agent Framework)
 * @param description Human-readable description of the agent's purpose
 * @param tags Set of tags categorizing the agent's capabilities or domain
 * @param generatedAt Timestamp when the agent was generated
 * @param llmModel LLM model used for generation
 * @param generationDurationMs Time taken to generate the agent in milliseconds
 */
data class GenerationMetadata(
    val version: Semver = Semver("1.0.0"),
    val author: String = "Meta-Agent Framework",
    val description: String = "",
    val tags: Set<String> = emptySet(),
    val generatedAt: Instant = Instant.now(),
    val generatedBy: String = "MetaAgent",
    val llmModel: String = "claude-sonnet-4",
    val codeSize: Int = 0,
    val generationDurationMs: Long = 0
)

/**
 * Represents an external tool discovered during agent generation.
 * 
 * Contains comprehensive information about APIs and services that can be
 * integrated into generated agents through the tool autodiscovery process.
 * 
 * @param name Human-readable name of the discovered tool
 * @param apiUrl Base URL of the tool's API endpoint
 * @param apiType Type of API protocol (REST, GraphQL, SOAP, RPC)
 * @param authenticationRequired Whether the tool requires authentication
 * @param authenticationScheme Type of authentication scheme used by the tool
 * @param endpoints List of available API endpoints for this tool
 * @param integrationComplexity Estimated complexity of integrating this tool
 * @param analysisTimestamp When the tool discovery and analysis was performed
 */
data class DiscoveredTool(
    @field:NotBlank(message = "Tool name cannot be blank")
    val name: String,
    
    @field:NotBlank(message = "Tool API URL cannot be blank")
    val apiUrl: String,
    
    val apiType: ApiType,
    
    val authenticationRequired: Boolean = true,
    
    val authenticationScheme: AuthenticationScheme? = null,
    
    val endpoints: List<ApiEndpoint> = emptyList(),
    
    val integrationComplexity: IntegrationComplexity = IntegrationComplexity.MEDIUM,
    
    val analysisTimestamp: Instant = Instant.now()
)

/**
 * Represents a specific API endpoint within a discovered tool.
 * 
 * Contains detailed information about individual API operations that can be
 * called from generated agent actions, including parameters and response types.
 * 
 * @param path URL path of the endpoint relative to the API base URL
 * @param method HTTP method used to access this endpoint
 * @param operation Descriptive name of the operation performed by this endpoint
 * @param description Human-readable description of the endpoint's functionality
 * @param parameters List of parameters accepted by this endpoint
 * @param responseType Expected response data type from this endpoint
 */
data class ApiEndpoint(
    val path: String,
    val method: HttpMethod,
    val operation: String,
    val description: String = "",
    val parameters: List<EndpointParameter> = emptyList(),
    val responseType: String = "Object"
)

/**
 * Represents a parameter for an API endpoint.
 * 
 * Defines the structure and constraints of parameters that can be passed
 * to API endpoints when generating agent actions that interact with external tools.
 * 
 * @param name Name of the parameter as expected by the API
 * @param type Data type of the parameter (string, integer, boolean, etc.)
 * @param location Where the parameter should be placed in the HTTP request
 * @param required Whether this parameter is mandatory for the API call
 * @param description Human-readable description of the parameter's purpose
 */
data class EndpointParameter(
    val name: String,
    val type: String,
    val location: ParameterLocation,
    val required: Boolean = true,
    val description: String = ""
)

/**
 * Represents a domain-specific data entity for the generated agent.
 * 
 * Defines data structures that are specific to the agent's problem domain,
 * which will be generated as Kotlin/Java classes with appropriate validation.
 * 
 * @param name Name of the domain entity (will become class name)
 * @param properties List of properties/fields for this entity
 * @param validations List of validation rules to apply to this entity
 */
data class DomainEntity(
    @field:NotBlank(message = "Entity name cannot be blank")
    val name: String,
    
    val properties: List<EntityProperty> = emptyList(),
    
    val validations: List<String> = emptyList()
)

/**
 * Represents a property/field within a domain entity.
 * 
 * Defines the characteristics of individual data fields that will be
 * generated as properties in the domain entity classes.
 * 
 * @param name Name of the property (will become field name)
 * @param type Data type of the property (String, Int, Boolean, etc.)
 * @param nullable Whether this property can have null values
 * @param defaultValue Default value to assign if not provided
 * @param description Human-readable description of the property's purpose
 */
data class EntityProperty(
    val name: String,
    val type: String,
    val nullable: Boolean = false,
    val defaultValue: String? = null,
    val description: String = ""
)

/**
 * Generated action metadata for code generation with GOAP heuristics.
 * 
 * Provides additional context beyond the core Action interface for generating
 * proper method signatures, annotations, and documentation. Includes GOAP
 * planning heuristics for optimal action selection and sequencing.
 * 
 * @param httpMethod HTTP method if this action corresponds to a REST endpoint
 * @param endpoint API endpoint path if applicable
 * @param toolReference Reference to discovered tool providing this capability
 * @param generatedImplementation Generated method implementation code
 * @param requiredImports Set of imports required for this action
 * @param goapHeuristics GOAP planning heuristics for cost and value optimization
 */
data class ActionGenerationInfo(
    val httpMethod: HttpMethod? = null,
    val endpoint: String? = null,
    val toolReference: String? = null,
    val generatedImplementation: String = "",
    val requiredImports: Set<String> = emptySet(),
    val goapHeuristics: GoapActionHeuristics = GoapActionHeuristics()
)

/**
 * Generated condition metadata for code generation.
 * 
 * Provides context for generating explicit condition implementations with
 * proper cost assignments and evaluation logic.
 * 
 * @param evaluationLogic Generated condition evaluation implementation
 * @param costJustification Explanation for the assigned cost value
 * @param requiredImports Set of imports required for this condition
 */
data class ConditionGenerationInfo(
    val evaluationLogic: String = "",
    val costJustification: String = "",
    val requiredImports: Set<String> = emptySet()
)

/**
 * HTTP methods supported for API endpoint interactions.
 * 
 * Defines the standard HTTP verbs that can be used when generating
 * agent actions that interact with discovered tool APIs.
 */
enum class HttpMethod {
    /** HTTP GET method for retrieving data */
    GET,
    
    /** HTTP POST method for creating new resources */
    POST,
    
    /** HTTP PUT method for updating/replacing entire resources */
    PUT,
    
    /** HTTP PATCH method for partial resource updates */
    PATCH,
    
    /** HTTP DELETE method for removing resources */
    DELETE,
    
    /** HTTP HEAD method for retrieving headers only */
    HEAD,
    
    /** HTTP OPTIONS method for discovering allowed operations */
    OPTIONS
}

/**
 * Types of API protocols supported by discovered tools.
 * 
 * Categorizes external APIs by their architectural style and protocol,
 * enabling appropriate integration strategies for each type.
 */
enum class ApiType {
    /** RESTful APIs using HTTP with standard verbs and resource URIs */
    REST,
    
    /** GraphQL APIs with flexible query language and single endpoint */
    GRAPHQL,
    
    /** SOAP APIs using XML messaging with WSDL definitions */
    SOAP,
    
    /** Remote Procedure Call APIs with direct method invocation */
    RPC
}

/**
 * Authentication schemes supported by discovered tools.
 * 
 * Defines the various authentication methods that external APIs might
 * require, enabling appropriate credential handling in generated agents.
 */
enum class AuthenticationScheme {
    /** API key authentication using custom headers or query parameters */
    API_KEY,
    
    /** Bearer token authentication using Authorization header */
    BEARER_TOKEN,
    
    /** OAuth 2.0 authentication with token exchange flow */
    OAUTH2,
    
    /** HTTP Basic Authentication using username/password */
    BASIC_AUTH,
    
    /** No authentication required for public APIs */
    NONE
}

/**
 * Complexity levels for integrating discovered tools into agents.
 * 
 * Estimates the effort required to integrate external tools based on
 * API complexity, authentication requirements, and data transformation needs.
 */
enum class IntegrationComplexity {
    /** Simple REST APIs with basic authentication and straightforward data */
    LOW,
    
    /** Standard APIs with moderate complexity in data structures or auth */
    MEDIUM,
    
    /** Complex APIs requiring significant data transformation or custom logic */
    HIGH,
    
    /** Highly complex APIs with extensive configuration or unusual protocols */
    VERY_HIGH
}

/**
 * Locations where API parameters can be placed in HTTP requests.
 * 
 * Defines where parameter values should be included when making
 * API calls from generated agent actions.
 */
enum class ParameterLocation {
    /** Parameter embedded in the URL path (e.g., /users/{id}) */
    PATH,
    
    /** Parameter included in URL query string (e.g., ?name=value) */
    QUERY,
    
    /** Parameter sent as HTTP header (e.g., X-Custom-Header: value) */
    HEADER,
    
    /** Parameter included in the HTTP request body */
    BODY
}

/**
 * GOAP heuristics for action planning optimization.
 * 
 * Contains cost and value metrics used by the GOAP planning system
 * to determine optimal action sequences. Values are normalized to [0,1].
 * 
 * @param cost Execution cost of the action (0=cheap, 1=expensive)
 * @param value Strategic value of performing this action (0=low, 1=high)
 * @param canRerun Whether this action can be executed multiple times
 * @param estimatedDurationMs Estimated execution time in milliseconds
 */
data class GoapActionHeuristics(
    val cost: Double = 0.5,
    val value: Double = 0.5,
    val canRerun: Boolean = true,
    val estimatedDurationMs: Long = 1000
)

/**
 * GOAP heuristics for condition evaluation optimization.
 * 
 * Contains cost metrics used by the GOAP planning system to optimize
 * condition evaluation order and frequency. Lower cost conditions
 * are evaluated more frequently.
 * 
 * @param cost Evaluation cost of the condition (0=cheap, 1=expensive)
 * @param cacheable Whether condition results can be cached temporarily
 * @param cacheTimeoutMs How long cached results remain valid
 */
data class GoapConditionHeuristics(
    val cost: Double = 0.1,
    val cacheable: Boolean = true,
    val cacheTimeoutMs: Long = 5000
)

/**
 * Build system types supported for code generation.
 * 
 * Defines the various build systems that can be used to compile and
 * manage dependencies for generated agents, enabling flexible project
 * setup based on user preferences and target environments.
 */
enum class BuildSystemType {
    /** Apache Maven build system using pom.xml configuration */
    MAVEN,
    
    /** Gradle build system using build.gradle or build.gradle.kts */
    GRADLE,
    
    /** SBT (Scala Build Tool) using build.sbt configuration */
    SBT
}

/**
 * Generated source code representation containing actual Kotlin/Java/Scala files.
 * 
 * This class represents the actual generated source code files that can be
 * written to disk or compiled. It maintains a reference to the internal
 * GeneratedAgentModel while containing the concrete source code implementation.
 * 
 * @param model Reference to the internal agent model representation
 * @param sourceFiles Map of filename to source code content for all generated files
 * @param mainAgentFile Source code of the main agent class
 * @param testFiles Map of test filename to test source code content
 * @param buildConfigurations Map of build system types to their configuration content
 * @param targetLanguage Programming language used for code generation
 * @param generatedTimestamp When the source code was generated
 */
data class GeneratedSourceCode(
    @field:Valid
    val model: GeneratedAgentModel,
    
    @field:NotEmpty(message = "Source files cannot be empty")
    val sourceFiles: Map<String, String>,
    
    @field:NotBlank(message = "Main agent file source code cannot be blank")
    val mainAgentFile: String,
    
    val testFiles: Map<String, String> = emptyMap(),
    
    val buildConfigurations: Map<BuildSystemType, String> = emptyMap(),
    
    val targetLanguage: TargetLanguage,
    
    val generatedTimestamp: Instant = Instant.now()
) {
    
    /**
     * Get the main agent class filename based on target language.
     */
    val mainAgentFileName: String
        get() = "${model.agentName}.${targetLanguage.fileExtension}"
    
    /**
     * Get all file names including source, test, and build files.
     */
    val allFileNames: Set<String>
        get() = sourceFiles.keys + testFiles.keys + getBuildFileNames()
    
    /**
     * Get the total lines of code generated.
     */
    val totalLinesOfCode: Int
        get() = (sourceFiles.values + testFiles.values).sumOf { it.lines().size }
    
    /**
     * Get build configuration filenames for all available build systems.
     */
    fun getBuildFileNames(): Set<String> = buildConfigurations.keys.map { buildSystemType ->
        when (buildSystemType) {
            BuildSystemType.MAVEN -> "pom.xml"
            BuildSystemType.GRADLE -> when (targetLanguage) {
                TargetLanguage.KOTLIN -> "build.gradle.kts"
                TargetLanguage.JAVA -> "build.gradle"
                TargetLanguage.SCALA -> "build.gradle"
            }
            BuildSystemType.SBT -> "build.sbt"
        }
    }.toSet()
    
    /**
     * Get the primary build configuration filename based on target language defaults.
     */
    fun getPrimaryBuildFileName(): String = when (targetLanguage) {
        TargetLanguage.KOTLIN -> if (buildConfigurations.containsKey(BuildSystemType.GRADLE)) "build.gradle.kts" else "pom.xml"
        TargetLanguage.JAVA -> "pom.xml"
        TargetLanguage.SCALA -> "build.sbt"
    }
    
    /**
     * Get build configuration content for a specific build system type.
     */
    fun getBuildConfiguration(buildSystemType: BuildSystemType): String? = buildConfigurations[buildSystemType]
    
    /**
     * Check if a specific build system is supported.
     */
    fun supportsBuildSystem(buildSystemType: BuildSystemType): Boolean = buildConfigurations.containsKey(buildSystemType)
}

/**
 * Result of writing generated source files to disk.
 * 
 * Contains information about the files that were successfully written,
 * any errors encountered, and the file system locations where the
 * generated agent code was placed.
 * 
 * @param sourceCode Reference to the generated source code that was written
 * @param writtenFiles Map of logical filename to actual file path written
 * @param baseDirectory Base directory where files were written
 * @param errors List of any errors encountered during file writing
 * @param writtenTimestamp When the files were written to disk
 */
data class WrittenFiles(
    @field:Valid
    val sourceCode: GeneratedSourceCode,
    
    @field:NotEmpty(message = "Written files cannot be empty")
    val writtenFiles: Map<String, String>, // logical name -> actual file path
    
    @field:NotBlank(message = "Base directory cannot be blank")
    val baseDirectory: String,
    
    val errors: List<String> = emptyList(),
    
    val writtenTimestamp: Instant = Instant.now()
) {
    
    /**
     * Whether all files were written successfully without errors.
     */
    val isSuccessful: Boolean
        get() = errors.isEmpty()
    
    /**
     * Get the main agent file path.
     */
    val mainAgentFilePath: String?
        get() = writtenFiles[sourceCode.mainAgentFileName]
    
    /**
     * Get all written file paths.
     */
    val allWrittenPaths: Collection<String>
        get() = writtenFiles.values
}