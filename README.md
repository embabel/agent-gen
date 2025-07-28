![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Apache Tomcat](https://img.shields.io/badge/apache%20tomcat-%23F8DC75.svg?style=for-the-badge&logo=apache-tomcat&logoColor=black)
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![ChatGPT](https://img.shields.io/badge/chatGPT-74aa9c?style=for-the-badge&logo=openai&logoColor=white)
![JSON](https://img.shields.io/badge/JSON-000?logo=json&logoColor=fff)
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)

<img align="left" src="https://github.com/embabel/embabel-agent/blob/main/embabel-agent-api/images/315px-Meister_der_Weltenchronik_001.jpg?raw=true" width="180">

&nbsp;&nbsp;&nbsp;&nbsp;

&nbsp;&nbsp;&nbsp;&nbsp;


# 🧠 Meta-Agent

Meta-Agent demonstrates an **Agent-Native Recursive Architecture** where the meta-agent itself IS a real `@Agent` using the same patterns, annotations, and planning algorithms as the agents it generates. This creates a self-consistent, recursive framework for intelligent agent creation using Goal Oriented Action Planning (GOAP).

**🟡 Current Status**: **Commit 1 Complete** - Foundation with Agent-API Integration
- ✅ MetaAgent as `@Agent` with `@Action` methods  
- ✅ Agent-native recursive architecture established
- ✅ Type-safe GOAP action chaining via implicit conditions
- ✅ Embabel ecosystem integration (embabel-agent-api, shell, autonomy)
- ✅ Comprehensive KDoc documentation

**🎯 Next Steps**: Extended shell commands, blackboard state management, and gradual method implementation.

**Key Innovation: Agent-Native Recursive Architecture** - The meta-agent IS an agent, using identical patterns to what it generates, creating unprecedented architectural consistency and self-improvement capabilities.

## 📋 Table of Contents

- [✨ Key Features](#-key-features)
- [🚀 Competitive Analysis & Key Innovations](#-competitive-analysis--key-innovations)
  - [Competitors in Agent Generation Space](#competitors-in-agent-generation-space)
  - [🏆 Meta-Agent Key Innovations](#-meta-agent-key-innovations)
  - [🎯 Unique Value Proposition](#-unique-value-proposition)
- [🧩 Project Structure](#-project-structure)
- [🔧 Meta-Agent Core Components](#-meta-agent-core-components)
  - [MetaAgent Core Engine](#metaagent-core-engine)
  - [MetaAgentService Generation Engine](#metaagentservice-generation-engine)
  - [Interactive Shell Application](#interactive-shell-application)
  - [Architecture Summary](#architecture-summary)
- [⚙️ Model Provider Configuration](#️-model-provider-configuration)
- [🚀 Usage Examples](#-usage-examples)
  - [Interactive Shell Session](#interactive-shell-session)
- [🔍 Intelligent Tool Autodiscovery Architecture](#-intelligent-tool-autodiscovery-architecture)
  - [Core Innovation: Production-Ready Tool Discovery](#core-innovation-production-ready-tool-discovery)
  - [Production Tool Discovery Flow](#production-tool-discovery-flow)
  - [Architecture Components](#architecture-components)
  - [Integration with Meta-Agent Generation](#integration-with-meta-agent-generation)
- [🎯 Smart Defaults: User-Provided vs Auto-Discovery](#-smart-defaults-user-provided-vs-auto-discovery)
  - [Intelligent Tool Selection Logic](#intelligent-tool-selection-logic)
  - [Real-World Usage Examples](#real-world-usage-examples)
  - [Configuration Examples](#configuration-examples)
  - [Shell Command Behavior](#shell-command-behavior)
- [🔧 Development Pipeline for Generated Agents](#-development-pipeline-for-generated-agents)
  - [Generated Agent Lifecycle](#generated-agent-lifecycle)
- [🔍 Generated Agent Agentic Audit Framework](#-generated-agent-agentic-audit-framework) *(Sprint 5 - Advanced Feature)*
- [Benefits of Concrete Type-Safe Generation](#benefits-of-concrete-type-safe-generation)

---

## ✨ Key Features

### 🎯 MVP Features (Sprint 1-2)
✅ **Interactive Shell Interface** - Claude Code-style CLI for agent generation  
✅ **Embabel Agent Integration** - Generates code using proven agent framework  
✅ **Template-Driven Generation** - Structural patterns, not domain-specific  
✅ **Spring Boot Integration** - Complete project structure generation  
✅ **Basic LLM Integration** - Uses configured model providers for code generation

### 🚀 Advanced Features (Post-MVP)
🔄 **Intelligent Tool Autodiscovery** - Automatically finds and integrates relevant APIs *(Sprint 3)*  
🔄 **Multi-Language JVM Support** - Kotlin, Java, Scala with full interoperability *(Sprint 4)*  
🔄 **Google A2A Protocol** - Agent-to-agent communication patterns *(Sprint 4)*  
🔄 **Agent Parallelism** - Concurrent execution and coordination patterns *(Sprint 4)*  
🔄 **Agentic Audit Framework** - Comprehensive tracking and self-evolution *(Sprint 5)*  

---

## 🚀 Competitive Analysis & Key Innovations

### Competitors in Agent Generation Space

#### **1. AutoGen (Microsoft)**
- **Strengths**: Multi-agent conversations, group chat patterns
- **Limitations**: Python-only, requires manual tool integration, no automatic API discovery
- **Focus**: Agent orchestration and conversation management

#### **2. LangChain Agents**
- **Strengths**: Extensive tool ecosystem, community support
- **Limitations**: Python-only, complex configuration, manual tool selection, no type-safe generation
- **Focus**: Chain-based agent workflows with pre-built tools

#### **3. Semantic Kernel (Microsoft)**
- **Strengths**: Multi-language support (.NET, Java, Python), plugin architecture
- **Limitations**: Requires manual skill/function development, no automatic API discovery, complex enterprise setup, limited production tooling
- **Focus**: Skill-based agent development with manual integration

#### **4. AgentGPT / AutoGPT**
- **Strengths**: Autonomous task execution, web-based interface  
- **Limitations**: No systematic API integration, lacks production deployment, prone to infinite loops, high token consumption, no code generation capabilities
- **Focus**: Autonomous web agents for general tasks

#### **5. CrewAI**
- **Strengths**: Role-based agent teams, task delegation
- **Limitations**: Manual tool configuration, no automatic API discovery, requires predefined roles and tasks, limited to predefined workflows
- **Focus**: Multi-agent collaboration with predefined roles

---

### 🏆 Meta-Agent Key Innovations

#### **1. Zero-Knowledge API Discovery**
**Innovation**: Automatically discovers and integrates ANY well-documented API without domain knowledge.

```kotlin
// Competitor approach (manual)
val tools = listOf(
    OpenTableTool(apiKey = "..."),
    StripeTool(apiKey = "..."),
    HereTool(apiKey = "...")
)

// Meta-Agent approach (automatic)
val agent = metaAgent.generate("Create restaurant booking agent")
// → Automatically discovers OpenTable, Resy, Stripe, HERE APIs
// → Tests API connectivity, generates integration code
// → Creates type-safe domain objects and validation
```

#### **2. Production-Ready Code Generation**
**Innovation**: Generates complete, deployable applications with testing, documentation, and CI/CD.

**Competitors** generate incomplete code snippets or require extensive manual work.
**Meta-Agent** generates:
- ✅ Complete Spring Boot applications with @Agent annotations
- ✅ Type-safe domain objects with validation
- ✅ Comprehensive test suites (unit, integration, API tests)
- ✅ Environment setup scripts and Docker configurations
- ✅ API documentation and usage examples

#### **3. Multi-Language JVM Support**
**Innovation**: Generates agents in Kotlin, Java, or Scala based on project requirements.

**Competitors** are typically locked to one language (Python/JavaScript).
**Meta-Agent** adapts to existing codebases and developer preferences.

#### **4. Intelligent Build Pipeline Recovery**
**Innovation**: Automatically fixes compilation errors and build failures with multiple recovery strategies.

```kotlin
enum class RecoveryStrategy {
    AUTO,        // Automatically fix common errors
    INTERACTIVE, // Guide user through fixes
    SMART,       // LLM-powered error analysis and fixes
    NONE         // Fail fast for debugging
}
```

#### **5. Deep API Introspection**
**Innovation**: Analyzes OpenAPI/Swagger specs to understand exact endpoint capabilities and generates optimal integration code.

**Competitors** use generic HTTP clients or require manual API integration.
**Meta-Agent**:
- ✅ Parses OpenAPI 3.0/2.0, Swagger, GraphQL schemas
- ✅ Maps endpoints to business functions automatically
- ✅ Generates type-safe client code with proper error handling
- ✅ Tests API connectivity and validates responses

#### **6. Human-in-the-Loop Credential Management**
**Innovation**: Guides developers through API key registration with testing and validation.

**Competitors** assume API keys are already available or handle them poorly.
**Meta-Agent**:
- ✅ Detects authentication requirements automatically
- ✅ Provides registration URLs and setup instructions
- ✅ Tests API connectivity before code generation
- ✅ Generates secure environment management scripts

#### **7. Embabel Agent Ecosystem Integration**
**Innovation**: Generates agents compatible with Embabel's production-ready agent framework.

- ✅ **@Agent, @Action, @AchievesGoal annotations** for structured agent development
- ✅ **Google A2A protocol** for agent-to-agent communication
- ✅ **Multi-provider LLM support** (OpenAI, Anthropic, Bedrock, Ollama)
- ✅ **Spring Boot integration** with @EnableAgentShell

#### **8. Domain-Agnostic Architecture**
**Innovation**: Meta-agent contains zero domain knowledge - all domain expertise is generated.

**Competitors** often hardcode domain-specific logic or require domain expertise.
**Meta-Agent**:
- ✅ Generic HTTP operation analysis (GET, POST, PUT, DELETE)
- ✅ Semantic matching via LLM without domain assumptions
- ✅ Domain objects and business logic generated per agent
- ✅ Extensible to any domain without code changes

---

### 🎯 Unique Value Proposition

**"From Idea to Production in Minutes"**

1. **Developer describes what they want** (natural language)
2. **Meta-Agent discovers relevant APIs** (automatic)
3. **Guides through credential setup** (human-in-the-loop)
4. **Generates production-ready code** (type-safe, tested)
5. **Provides deployment scripts** (Docker, environment setup)
6. **Agent is ready for production** (complete application)

**No other solution** combines automatic API discovery, production-ready code generation, multi-language support, and intelligent error recovery in a single platform.

---

## 🧩 Project Structure

This is a **multi-module Maven project** with integrated scripts:

```
meta-agent/
├── scripts/                   # Development and deployment scripts
│   ├── setup-env.sh           # Environment setup script
│   ├── setup-env.ps1          # PowerShell environment setup
│   ├── run-dev.sh             # Development mode startup
│   ├── run-prod.sh            # Production mode startup
│   ├── build-all.sh           # Build all modules
│   └── test-all.sh            # Run all tests
├── meta-agent-core/           # Core generation framework
├── meta-agent-service/        # Interactive shell application  
│   └── scripts/               # Generated agent script templates
│       ├── env-template.sh    # Environment template
│       └── env-template.ps1   # PowerShell environment template
├── meta-agent-examples/       # Generated agent samples
│   └── generated/             # Generated agent outputs
│       ├── agent-1/           # Example generated agent
│       │   ├── src/main/kotlin/
│       │   ├── scripts/
│       │   │   ├── setup-env.sh     # Agent-specific environment setup
│       │   │   └── setup-env.ps1    # PowerShell version
│       │   └── pom.xml
│       └── agent-2/           # Another example
└── pom.xml                    # Parent Maven configuration
```

---

## ⚙️ Model Provider Configuration

The Meta-Agent uses **embabel-agent-api** for model provider configuration, supporting multiple LLM providers:

```properties
# application.properties (configured via embabel-agent-api)

# OpenAI Configuration
embabel.agent.model.provider=openai
embabel.agent.model.openai.api-key=${OPENAI_API_KEY}
embabel.agent.model.openai.model=gpt-4

# Anthropic Configuration  
embabel.agent.model.provider=anthropic
embabel.agent.model.anthropic.api-key=${ANTHROPIC_API_KEY}
embabel.agent.model.anthropic.model=claude-3-sonnet-20240229

# AWS Bedrock Configuration
embabel.agent.model.provider=bedrock
embabel.agent.model.bedrock.region=us-west-2
embabel.agent.model.bedrock.model=anthropic.claude-3-sonnet-20240229-v1:0

# Hugging Face Configuration
embabel.agent.model.provider=huggingface
embabel.agent.model.huggingface.api-key=${HUGGINGFACE_API_KEY}
embabel.agent.model.huggingface.model=microsoft/DialoGPT-large

# Local Ollama Configuration
embabel.agent.model.provider=ollama
embabel.agent.model.ollama.base-url=http://localhost:11434
embabel.agent.model.ollama.model=llama2

# Docker Ollama Configuration
embabel.agent.model.provider=ollama
embabel.agent.model.ollama.base-url=http://ollama-container:11434
embabel.agent.model.ollama.model=codellama
```

**Usage in Meta-Agent:**
```kotlin
@Service
class MetaAgentService(
    private val metaAgent: MetaAgent,          // Core generation engine
    private val properties: MetaAgentProperties
) {
    
    fun generateAgent(context: MetaAgentContext): GeneratedAgentModel {
        val sessionId = UUID.randomUUID().toString()
        
        logger.info("Starting agent generation session: $sessionId")
        
        return try {
            // Service delegates to the core MetaAgent engine
            val generatedAgent = metaAgent.generateAgent(context)
            
            logger.info("Agent generation completed: $sessionId")
            
            generatedAgent
        } catch (e: Exception) {
            logger.error("Agent generation failed: $sessionId", e)
            throw MetaAgentGenerationException("Generation failed: ${e.message}", e)
        }
    }
}

// The MetaAgent core engine handles ModelProvider internally
@Component  
class MetaAgent(
    private val modelProvider: ModelProvider  // Auto-configured by embabel-agent-api
) {
    fun generateAgent(context: MetaAgentContext): GeneratedAgentModel {
        val llm = modelProvider.defaultLlm()
        // ... LLM processing logic here
    }
}
```

---

## 🔧 Meta-Agent Core Components

The Meta-Agent architecture consists of **two main layers** within the `meta-agent-service` Spring Boot application:

1. **MetaAgentService** - Core business logic for agent generation
2. **Interactive Shell Application** - CLI interface that orchestrates the service

Both components live in the `meta-agent-service` module as a single Spring Boot application.

### MetaAgent Core Engine

**Key Innovation:** The **MetaAgent itself IS a real agent** using `@Agent` and `@Action` annotations - the same patterns as the agents it generates.

```kotlin
@Agent(
    name = "MetaAgent",
    description = "Generates other agents through goal-oriented planning",
    planner = Planner.GOAP
)
@Component
class MetaAgent {
    
    @AchievesGoal(description = "Create agent specification from user input")
    @Action(
        cost = 0.4, value = 0.9,
        toolGroups = ["llm", "design"]
    )
    fun createAgentSpecification(userInput: UserInput): AgentSpecification {
        return try {
            // LLM-based extraction for structured specification
            processFreeTextInput(userInput)
        } catch (e: Exception) {
            // Semantic extraction fallback with smart parsing
            createFallbackSpecification(userInput)
        }
    }
    
    @AchievesGoal(description = "Generate complete agent code with annotations")
    @Action(
        cost = 0.5, value = 1.0,
        toolGroups = ["codegen", "templates"]
    )
    fun generateAgent(specification: AgentSpecification): GeneratedAgentModel {
        logger.info("MetaAgent core generation started for: ${specification.domain}")
        
        // Step 1: Transform specification to generation context
        val context = createGenerationContext(specification)
        
        // Step 2: Discover and analyze relevant tools
        val tools = discoverTools(specification)
        
        // Step 3: Engineer sophisticated prompts
        val prompts = promptEngineeringService.buildPrompts(specification, tools)
        
        // Step 4: Generate code using LLM
        val generatedCode = generateCodeWithLLM(prompts, specification)
        
        // Step 5: Validate and refine
        val validatedCode = validator.validateAndRefine(generatedCode)
        
        // Step 6: Create complete agent model
        return GeneratedAgentModel(
            agent = createAgentFromSpecification(specification),
            packageName = specification.suggestedPackageName(),
            discoveredTools = tools,
            generationMetadata = GenerationMetadata(),
            generationContext = context
        )
    }
    
    @Action(cost = 0.7, value = 0.8, toolGroups = ["web", "apis", "rag"])
    fun discoverTools(specification: AgentSpecification): List<DiscoveredTool> { ... }
    
    @Action(cost = 0.4, value = 0.7, toolGroups = ["aop", "audit"])
    fun makeAuditAware(agent: GeneratedAgentModel): GeneratedAgentModel { ... }
    
    private fun generateCodeWithLLM(prompts: PromptContext, specification: AgentSpecification): CodeResult {
        val llm = modelProvider.defaultLlm()
        
        // Use LLM to generate agent code with embabel-agent-api annotations
        return llm.createObject(
            prompts.masterPrompt,
            CodeResult::class.java
        )
    }
}
```

### MetaAgentService Generation Engine

The **MetaAgentService** is the core business logic service that handles all agent generation functionality. It runs within the Spring Boot application and is used by the shell commands. The service orchestrates the actual **MetaAgent** core engine.

```kotlin
// meta-agent-service/src/main/kotlin/com/embabel/metaagent/service/MetaAgentService.kt
@Service
class MetaAgentService(
    private val metaAgent: MetaAgent,                           // The core generation engine
    private val properties: MetaAgentProperties                  // Service configuration
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(MetaAgentService::class.java)
    }
    
    /**
     * Main agent generation orchestration with comprehensive pipeline
     */
    fun generateAgent(context: MetaAgentContext): GeneratedAgentModel {
        val sessionId = UUID.randomUUID().toString()
        
        logger.info("Starting agent generation session: $sessionId")
        
        return try {
            // Service delegates to the core MetaAgent engine
            val generatedAgent = metaAgent.generateAgent(context)
            
            // Service-layer post-processing (logging, validation, etc.)
            logger.info("Agent generation completed successfully: $sessionId")
            
            generatedAgent
            
        } catch (e: Exception) {
            logger.error("Agent generation failed: $sessionId", e)
            throw MetaAgentGenerationException("Agent generation failed: ${e.message}", e)
        }
    }
    
    /**
     * Additional service-layer methods for specific functionality
     */
    fun validateAgentConfiguration(context: MetaAgentContext): ValidationResult {
        return metaAgent.validateConfiguration(context)
    }
    
    fun estimateGenerationComplexity(context: MetaAgentContext): GenerationComplexity {
        return metaAgent.estimateComplexity(context)
    }
}

// Supporting data classes for the comprehensive generation pipeline

data class IntentAnalysisResult(
    val semanticIntent: SemanticIntent,
    val discoveredTools: List<DiscoveredTool>,
    val toolCapabilities: List<ToolCapability>,
    val endpointMappings: List<EndpointMapping>,
    val complexity: GenerationComplexity
)

data class SemanticIntent(
    val primaryPurpose: String,
    val requiredCapabilities: List<String>,
    val dataFlowPatterns: List<String>,
    val integrationRequirements: List<String>,
    val performanceRequirements: List<String>,
    val securityRequirements: List<String>,
    val userInteractionPatterns: List<String>,
    val errorHandlingRequirements: List<String>
)

data class ToolCapability(
    val toolName: String,
    val apiAnalysis: ApiAnalysis,
    val endpointCapabilities: List<EndpointCapability>,
    val authenticationFlow: AuthenticationFlow,
    val rateLimiting: RateLimitingInfo,
    val errorHandling: ErrorHandlingStrategy,
    val dataModels: List<DataModel>,
    val integrationComplexity: IntegrationComplexity,
    val testingStrategy: TestingStrategy
)

data class PromptEngineeringContext(
    val userContext: MetaAgentContext,
    val semanticIntent: SemanticIntent,
    val toolCapabilities: List<ToolCapability>,
    val templateStrategy: TemplateStrategy,
    val codePatterns: List<CodePattern>,
    val validationRules: List<ValidationRule>,
    val targetLanguage: TargetLanguage,
    val sessionId: String
)

data class CodeGenerationResult(
    val agentClass: String,
    val actionMethods: List<String>,
    val domainEntities: List<String>,
    val serviceClasses: List<String>,
    val configurationClasses: List<String>,
    val sourceCode: String,
    val actions: List<AgentAction>,
    val goals: List<AgentGoal>,
    val domainEntities: List<DomainEntity>
)

data class EnhancedGenerationResult(
    val sourceCode: String,
    val testCode: String,
    val documentation: String,
    val buildConfig: String,
    val actions: List<AgentAction>,
    val goals: List<AgentGoal>,
    val domainEntities: List<DomainEntity>
)

enum class GenerationComplexity {
    SIMPLE, MODERATE, COMPLEX, VERY_COMPLEX
}

enum class TemplateStrategy {
    BASIC, ADVANCED, ENTERPRISE, CUSTOM
}

data class CodePattern(
    val name: String,
    val pattern: String,
    val applicability: List<String>
)

data class ValidationRule(
    val name: String,
    val rule: String,
    val severity: ValidationSeverity
)

enum class ValidationSeverity {
    ERROR, WARNING, INFO
}

class MetaAgentGenerationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

// Input specification - Generic and extensible
data class MetaAgentContext(
    val task: String,                               // "restaurant reservation"
    val language: String = "kotlin",                // kotlin|java|scala
    val toolGroups: List<String> = emptyList(),     // Manual tool specification
    val properties: Map<String, Any> = emptyMap(),  // Generic properties
    val constraints: List<String> = emptyList(),    // Business constraints
    val integrations: List<String> = emptyList(),   // Required integrations
    val outputFormat: String? = null,               // JSON, XML, etc.
    val securityLevel: SecurityLevel = SecurityLevel.STANDARD,
    val forceDiscovery: Boolean = false,            // Override discovery settings
) {
    
    // Convenience accessors for technical properties only
    val maxResults: Int? get() = properties["maxResults"] as? Int
    val timeout: String? get() = properties["timeout"] as? String
    val a2aEnabled: Boolean get() = properties["a2aEnabled"] as? Boolean ?: false
    
    // Generic context builder - no domain-specific builders
    companion object {
        fun create(
            task: String,
            properties: Map<String, Any> = emptyMap()
        ) = MetaAgentContext(
            task = task,
            properties = properties
        )
    }
}

enum class SecurityLevel {
    BASIC,      // No sensitive data
    STANDARD,   // Normal business data
    HIGH,       // PII, financial data
    CRITICAL    // Highly sensitive
}

// Generation output
data class GeneratedAgentModel(
    val sourceCode: String,                     // Complete agent class
    val projectStructure: GeneratedAgentProjectStructure,    // pom.xml, config files
    val testCode: String? = null,              // Generated tests
)
```

### Interactive Shell Application

The **Interactive Shell Application** is the main Spring Boot application that provides the CLI interface. It contains and orchestrates the MetaAgentService to provide interactive agent generation commands.

```kotlin
// meta-agent-service/src/main/kotlin/com/embabel/metaagent/MetaAgentShellApplication.kt
@SpringBootApplication
@EnableAgentShell                              // Embabel shell integration
@EnableAgents(
    loggingTheme = LoggingThemes.SEVERANCE,    // Terminal theming
    mcpServers = [McpServers.DOCKER]           // Tool integration
)
class MetaAgentShellApplication

fun main(args: Array<String>) {
    runApplication<MetaAgentShellApplication>(*args)
}

// Shell commands that use MetaAgentService for agent generation
@ShellComponent
class MetaAgentCommands(
    private val metaAgentService: MetaAgentService,    // Uses the service layer
    private val terminalServices: TerminalServices,
) {
    
    @ShellMethod("Generate agent from description", key = ["generate", "gen"])
    fun generateAgent(
        @ShellOption(help = "What should the agent do?") userPrompt: String,
        @ShellOption(value = ["--language", "-l"]) language: String = "kotlin",
        @ShellOption(value = ["--tools", "-t"]) tools: List<String> = emptyList(),
        @ShellOption(value = ["--a2a", "-a"]) a2a: Boolean = false,
        @ShellOption(value = ["--test-framework", "-tf"]) testFramework: String = "junit5",
        @ShellOption(value = ["--doc-type", "-dt"]) documentationType: String = "markdown",
        @ShellOption(value = ["--build-tool", "-bt"]) buildTool: String = "maven",
        @ShellOption(value = ["--spring-version", "-sv"]) springVersion: String = "3.2.0",
        @ShellOption(value = ["--kotlin-version", "-kv"]) kotlinVersion: String = "2.1.0",
        @ShellOption(value = ["--java-version", "-jv"]) javaVersion: String = "21"
    ): String {
        val context = MetaAgentContext(
            userInput = UserInput(userPrompt),
            targetLanguage = TargetLanguage.fromString(language),
            generationSettings = GenerationSettings(
                includeTests = true,
                includeDocumentation = true,
                testFramework = when(testFramework.lowercase()) {
                    "junit5" -> TestFramework.JUNIT5
                    "junit4" -> TestFramework.JUNIT4
                    "testng" -> TestFramework.TESTNG
                    "spock" -> TestFramework.SPOCK
                    "kotest" -> TestFramework.KOTEST
                    else -> TestFramework.forLanguage(TargetLanguage.fromString(language))
                },
                documentationType = DocumentationType.fromString(documentationType),
                buildTool = BuildTool.fromString(buildTool),
                springBootVersion = SpringBootVersion.fromString(springVersion),
                kotlinVersion = KotlinVersion.fromString(kotlinVersion),
                javaVersion = JavaVersion.fromString(javaVersion)
            )
        )
        
        // Use the service layer to generate the agent
        val generated = metaAgentService.generateAgent(context)
        return "✅ Generated ${context.targetLanguage} agent: ${generated.agentName}"
    }
    
    @ShellMethod("Interactive agent design session")
    fun design(): String {
        return terminalServices.chat(/* agent design chat session */)
    }
}
```

### Architecture Summary

**🏗️ Component Relationship:**
```
┌─────────────────────────────────────────────────────────────┐
│                    meta-agent-service                        │
│                   (Spring Boot App)                         │
│                                                             │
│  ┌─────────────────┐    calls    ┌─────────────────┐       │
│  │   Shell App     │────────────▶│  MetaAgentService│       │
│  │ (CLI Interface) │             │ (Business Logic) │       │
│  └─────────────────┘             └─────────────────┘       │
│                                           │                 │
│                                           │ calls           │
│                                           ▼                 │
└─────────────────────────────────────────────────────────────┘
                                           │
                                           │ calls
                                           ▼
                ┌─────────────────────────────────────────────┐
                │              meta-agent-core                │
                │                                             │
                │  ┌─────────────────┐  ┌─────────────────┐  │
                │  │   MetaAgent     │  │ Data Models &   │  │
                │  │ (Core Engine)   │  │   Interfaces    │  │
                │  │                 │  │ • MetaAgentContext│ │
                │  │ • LLM Processing│  │ • GeneratedAgent│  │
                │  │ • Tool Discovery│  │ • AuditFramework│  │
                │  │ • Code Generation│  │                 │  │
                │  └─────────────────┘  └─────────────────┘  │
                └─────────────────────────────────────────────┘
```

**🔄 Execution Flow:**
1. User runs shell command (e.g., `generate "restaurant booking agent"`)
2. **Shell App** creates MetaAgentContext with user input
3. **Shell App** calls `MetaAgentService.generateAgent(context)`
4. **MetaAgentService** calls `MetaAgent.generateAgent(context)`
5. **MetaAgent** orchestrates LLM, templates, validation, etc.
6. Generated agent flows back: **MetaAgent** → **MetaAgentService** → **Shell App** → **User**

---

## 🎯 Generated Code Examples

### Basic Restaurant Agent (Kotlin)

**Shell Command:**
```bash
meta-agent:> generate "Find vegan restaurants in Berlin" --language kotlin --location Berlin --tools WEB,MAPS
```

**Generated Code:**
```kotlin
// Generated: RestaurantFinderAgent.kt
@Agent(
    description = "Find vegan restaurants in Berlin with location awareness",
    scan = true
)
class RestaurantFinderAgent(
    private val restaurantService: RestaurantService,
    @Value("\${restaurant-finder.model:gpt-4.1-mini}") private val model: String,
    @Value("\${restaurant-finder.max-results:10}") private val maxResults: Int,
) {
    
    @Action
    fun extractPreferences(userInput: UserInput): DiningPreferences =
        usingModel(model).createObject(
            """
            Extract dining preferences focusing on vegan options from:
            ${userInput.content}
            """.trimIndent()
        )
    
    @Action(toolGroups = [CoreToolGroups.WEB, CoreToolGroups.MAPS])
    fun findVeganRestaurants(
        preferences: DiningPreferences,
        location: String = "Berlin"
    ): RestaurantResults =
        usingModel(model).createObject(
            """
            Find vegan restaurants in $location matching preferences:
            - Cuisine: ${preferences.cuisine}
            - Price range: ${preferences.priceRange}
            - Distance: within ${preferences.maxDistance}km
            
            Use web search and maps to find top $maxResults options.
            """.trimIndent()
        )
    
    @AchievesGoal(description = "Provide curated vegan restaurant recommendations")
    @Action  
    fun recommendRestaurants(
        restaurants: RestaurantResults,
        preferences: DiningPreferences
    ): RestaurantRecommendations =
        usingModel(model).createObject(
            """
            Rank and recommend the best vegan restaurants from:
            ${restaurants.results}
            
            Consider user preferences and provide detailed recommendations.
            """.trimIndent()
        )
}

// Generated data classes
data class DiningPreferences(
    val cuisine: String? = null,
    val priceRange: PriceRange,
    val maxDistance: Double = 5.0,
    val dietaryRestrictions: List<String> = listOf("vegan")
)

data class RestaurantResults(
    val results: List<Restaurant>,
    val totalFound: Int
)

data class RestaurantRecommendations(
    val recommendations: List<RankedRestaurant>,
    val reasoning: String
)
```

### A2A Travel Coordinator (Kotlin)

**Shell Command:**
```bash
meta-agent:> generate "Coordinate hotel and flight bookings" --a2a true --tools WEB
```

**Generated Code:**
```kotlin
// Generated: TravelCoordinatorAgent.kt
@Agent(description = "Coordinate travel bookings using agent collaboration")
class TravelCoordinatorAgent(
    private val a2aClient: A2AClient,
    @Value("\${travel.model:gpt-4.1-mini}") private val model: String,
) {
    
    @Action
    fun parseTravelRequest(userInput: UserInput): TravelRequest =
        usingModel(model).createObject(
            "Extract travel details from: ${userInput.content}"
        )
    
    @Action(toolGroups = [CoreToolGroups.WEB])
    suspend fun coordinateBookings(request: TravelRequest): TravelPlan {
        // Parallel agent communication via Google A2A
        val hotelTask = async {
            a2aClient.sendMessage(
                agentId = "hotel-booking-agent",
                message = HotelRequest(
                    destination = request.destination,
                    checkIn = request.checkIn,
                    checkOut = request.checkOut,
                    guests = request.guests
                )
            )
        }
        
        val flightTask = async {
            a2aClient.sendMessage(
                agentId = "flight-booking-agent", 
                message = FlightRequest(
                    origin = request.origin,
                    destination = request.destination,
                    departureDate = request.departureDate,
                    returnDate = request.returnDate,
                    passengers = request.guests
                )
            )
        }
        
        // Coordinate responses
        val hotelResults = hotelTask.await()
        val flightResults = flightTask.await()
        
        return TravelPlan(
            hotels = hotelResults,
            flights = flightResults,
            coordinatedAt = Instant.now()
        )
    }
    
    @AchievesGoal(description = "Complete coordinated travel booking")
    @Action
    fun finalizeBookings(plan: TravelPlan): BookingConfirmation =
        BookingConfirmation(
            hotelConfirmation = plan.hotels.confirmationNumber,
            flightConfirmation = plan.flights.confirmationNumber,
            totalCost = plan.totalCost
        )
}
```

### Java Payment Agent

**Shell Command:**
```bash
meta-agent:> generate "Process credit card payments" --language java --tools WEB
```

**Generated Code:**
```java
// Generated: PaymentProcessorAgent.java
@Agent(description = "Secure credit card payment processing")
@Component
public class PaymentProcessorAgent {
    
    @Autowired
    private PaymentService paymentService;
    
    @Value("${payment.model:gpt-4.1-mini}")
    private String model;
    
    @Action
    public PaymentRequest validatePayment(UserInput userInput) {
        return PromptRunner.usingModel(model).createObject(
            "Extract and validate payment details from: " + userInput.getContent(),
            PaymentRequest.class
        );
    }
    
    @Action(toolGroups = {CoreToolGroups.WEB})
    public PaymentVerification verifyCard(PaymentRequest request) {
        // Credit card validation logic
        return paymentService.verifyCard(request.getCardNumber());
    }
    
    @AchievesGoal(description = "Complete secure payment transaction")
    @Action
    public PaymentResult processPayment(
        PaymentRequest request,
        PaymentVerification verification
    ) {
        if (!verification.isValid()) {
            throw new PaymentException("Card verification failed");
        }
        
        return paymentService.processPayment(request);
    }
}
```

---

## 🚀 Usage Examples

### Interactive Shell Session
```bash
./mvnw spring-boot:run

    ███╗   ███╗███████╗████████╗ █████╗       █████╗  ██████╗ ███████╗███╗   ██╗████████╗
    ████╗ ████║██╔════╝╚══██╔══╝██╔══██╗     ██╔══██╗██╔════╝ ██╔════╝████╗  ██║╚══██╔══╝
    ██╔████╔██║█████╗     ██║   ███████║     ███████║██║  ███╗█████╗  ██╔██╗ ██║   ██║   
    ██║╚██╔╝██║██╔══╝     ██║   ██╔══██║     ██╔══██║██║   ██║██╔══╝  ██║╚██╗██║   ██║   
    ██║ ╚═╝ ██║███████╗   ██║   ██║  ██║     ██║  ██║╚██████╔╝███████╗██║ ╚████║   ██║   
    ╚═╝     ╚═╝╚══════╝   ╚═╝   ╚═╝  ╚═╝     ╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═══╝   ╚═╝   

meta-agent:> help
Available commands:
  generate (gen) - Generate agent from description
  design - Interactive agent design session  
  list - Show available tool groups
  validate - Validate generated agent code

meta-agent:> generate "Weather forecasting agent for London" --language kotlin --location London --tools WEB
🔄 Generating Kotlin agent...
✅ Generated WeatherForecastAgent.kt (247 lines)
✅ Created Spring Boot project structure  
✅ Added Maven dependencies for Embabel Agent API
✅ Generated application.yml configuration
✅ Created test class WeatherForecastAgentTest.kt

meta-agent:> design
🎨 Entering interactive design mode...
You: I need an agent that books restaurants and handles cancellations
Assistant: I'll create a restaurant management agent with booking and cancellation capabilities...
[Interactive chat session continues...]
```

---

## 🔍 Intelligent Tool Autodiscovery Architecture

### Core Innovation: Production-Ready Tool Discovery

**The Problem:** Users don't know which specific APIs exist for their needs (restaurant bookings, payment processing, location services, etc.). They just know what they want to accomplish. Additionally, production integration requires proper API introspection, credential management, and testing workflows for any API.

**The Solution:** Meta-agent automatically discovers and integrates the right APIs based on semantic understanding of the agent's purpose, with production-grade API analysis, human-in-the-loop credential setup, and comprehensive testing that works with any well-documented API.

### Production Tool Discovery Flow

```
User Input: "Create agent for [any domain]"
    ↓
LLM Analysis: Extract intent, capabilities, domain
    ↓
API Discovery: Search for relevant APIs in any domain
    ↓
API Introspection: Analyze OpenAPI/Swagger/GraphQL schemas
    ↓
Endpoint Analysis: Map specific endpoints to agent requirements
    ↓
Credential Detection: Identify authentication requirements
    ↓
Human-in-Loop: Guide user through API key registration
    ↓
Tool Testing: Validate API connectivity and responses
    ↓
Code Generation: Generate production-ready agent with tools
    ↓
Environment Setup: Create scripts for env variables/keys
    ↓
Complete Agent: Production-ready with full testing suite
```

### Architecture Components

> **Note**: The system is designed to work with **ANY well-documented API** across **ANY domain**. The restaurant, payment, and travel examples shown are illustrations of the generic introspection capabilities. The system automatically adapts to new APIs and domains without requiring code changes.

#### 1. RAG-Based API Document Analysis Service

```kotlin
// meta-agent-core/src/main/kotlin/com/embabel/metaagent/discovery/ApiDocumentAnalysisService.kt
@Service
class ApiDocumentAnalysisService(
    private val vectorStore: VectorStore,
    private val knowledgeGraph: ApiKnowledgeGraph,
    private val modelProvider: ModelProvider
) {
    
    data class ApiAnalysis(
        val apiName: String,
        val baseUrl: String,
        val specification: ApiSpecification,
        val authenticationScheme: AuthenticationScheme,
        val endpoints: List<EndpointDefinition>,
        val endpointCapabilities: List<EndpointCapability>,
        val documentation: ApiDocumentation,
        val dataModels: List<DataModel>,
        val rateLimiting: RateLimitInfo?,
        val testingStrategy: TestingStrategy
    )
    
    data class EndpointDefinition(
        val path: String,
        val method: String,
        val operationId: String?,
        val summary: String?,
        val description: String?,
        val parameters: List<ParameterDefinition>,
        val requestBody: RequestBodyDefinition?,
        val responses: List<ResponseDefinition>,
        val httpOperation: HttpOperation,
        val tags: List<String>
    )
    
    data class EndpointCapability(
        val endpointPath: String,
        val method: String,
        val httpOperation: HttpOperation,
        val dataOperations: List<DataOperation>,
        val integrationComplexity: IntegrationComplexity,
        val usagePatterns: List<UsagePattern>,
        val dependencies: List<String>
    )
    
    data class ApiDocumentation(
        val specificationUrl: String,
        val specType: ApiSpecification,
        val sections: MutableList<DocumentationSection>
    )
    
    data class DocumentationSection(
        val title: String,
        val content: String,
        val url: String,
        val type: DocumentationType
    )
    
    enum class HttpOperation {
        GET_LIST, GET_ITEM, POST_CREATE, PUT_UPDATE, PATCH_UPDATE, DELETE_ITEM, UNKNOWN
    }
    
    enum class DocumentationType {
        SWAGGER_UI, REDOC, MARKDOWN, HTML, API_REFERENCE, TUTORIAL, QUICKSTART
    }
    
    enum class ApiSpecification {
        OPENAPI_3_0, OPENAPI_2_0, GRAPHQL, REST_DISCOVERY, WADL, CUSTOM
    }
    
    enum class AuthenticationScheme {
        API_KEY, OAUTH2, BASIC_AUTH, BEARER_TOKEN, CUSTOM, NONE
    }
    
    fun analyzeApi(apiUrl: String): ApiAnalysis {
        // 1. Document Ingestion - Retrieve API documentation
        val documents = ingestApiDocumentation(apiUrl)
        
        // 2. Document Chunking - Break down into semantic units
        val chunks = chunkDocuments(documents)
        
        // 3. Embedding Generation - Create vector representations
        val embeddings = generateEmbeddings(chunks)
        vectorStore.store(embeddings)
        
        // 4. Entity Extraction - Identify API entities for knowledge graph
        val entities = extractApiEntities(documents)
        
        // 5. Relationship Mapping - Connect entities through typed relationships
        val relationships = mapEntityRelationships(entities)
        knowledgeGraph.addEntities(entities)
        knowledgeGraph.addRelationships(relationships)
        
        // 6. Hybrid Retrieval - Combine vector search with graph reasoning
        val hybridAnalysis = performHybridAnalysis(apiUrl)
        
        return hybridAnalysis
    }
    
    private fun performHybridAnalysis(apiUrl: String): ApiAnalysis {
        // RAG: Semantic similarity search
        val semanticMatches = vectorStore.similaritySearch(apiUrl)
        
        // Knowledge Graph: Relationship-based reasoning
        val graphInferences = knowledgeGraph.inferCapabilities(semanticMatches)
        
        // LLM: Augmented generation with both contexts
        return modelProvider.defaultLlm().createObject(
            """
            API Documentation Context: ${semanticMatches}
            Knowledge Graph Inferences: ${graphInferences}
            
            Analyze this API for agent integration capabilities.
            """.trimIndent()
        )
    }
    
    private fun parseOpenApi3WithDocumentation(apiUrl: String): List<EndpointDefinition> {
        val swaggerSpec = fetchSwaggerSpec(apiUrl)
        val endpoints = mutableListOf<EndpointDefinition>()
        
        swaggerSpec.paths.forEach { (path, pathItem) ->
            pathItem.operations.forEach { (method, operation) ->
                endpoints.add(EndpointDefinition(
                    path = path,
                    method = method.uppercase(),
                    operationId = operation.operationId,
                    summary = operation.summary,
                    description = operation.description,
                    parameters = operation.parameters?.map { param ->
                        ParameterDefinition(
                            name = param.name,
                            type = param.schema?.type ?: "string",
                            required = param.required,
                            description = param.description,
                            location = param.`in` // query, path, header, cookie
                        )
                    } ?: emptyList(),
                    requestBody = operation.requestBody?.let { body ->
                        RequestBodyDefinition(
                            required = body.required,
                            contentTypes = body.content.keys.toList(),
                            schema = extractSchemaDefinition(body.content.values.first().schema)
                        )
                    },
                    responses = operation.responses.map { (code, response) ->
                        ResponseDefinition(
                            statusCode = code,
                            description = response.description,
                            schema = response.content?.values?.firstOrNull()?.schema?.let { 
                                extractSchemaDefinition(it) 
                            }
                        )
                    },
                    httpOperation = analyzeHttpOperation(endpoint.method, endpoint.path),
                    tags = operation.tags ?: emptyList()
                ))
            }
        }
        
        return endpoints
    }
    
    private fun analyzeEndpointCapabilities(endpoints: List<EndpointDefinition>): List<EndpointCapability> {
        return endpoints.map { endpoint ->
            EndpointCapability(
                endpointPath = endpoint.path,
                method = endpoint.method,
                httpOperation = endpoint.httpOperation,
                dataOperations = determineDataOperations(endpoint),
                integrationComplexity = assessIntegrationComplexity(endpoint),
                usagePatterns = inferUsagePatterns(endpoint),
                dependencies = findEndpointDependencies(endpoint, endpoints)
            )
        }
    }
    
    private fun analyzeHttpOperation(method: String, path: String): HttpOperation {
        val pathLower = path.lowercase()
        
        return when (method.uppercase()) {
            "GET" -> when {
                pathLower.contains("{id}") || pathLower.matches(".*/.+$".toRegex()) -> HttpOperation.GET_ITEM
                else -> HttpOperation.GET_LIST
            }
            "POST" -> HttpOperation.POST_CREATE
            "PUT" -> HttpOperation.PUT_UPDATE
            "PATCH" -> HttpOperation.PATCH_UPDATE
            "DELETE" -> HttpOperation.DELETE_ITEM
            else -> HttpOperation.UNKNOWN
        }
    }
    
    private fun extractApiDocumentation(apiUrl: String, specType: ApiSpecification): ApiDocumentation {
        val documentation = ApiDocumentation(
            specificationUrl = apiUrl,
            specType = specType,
            sections = mutableListOf()
        )
        
        // Try to find additional documentation
        val docUrls = discoverDocumentationUrls(apiUrl)
        docUrls.forEach { docUrl ->
            try {
                val content = fetchDocumentationContent(docUrl)
                documentation.sections.add(DocumentationSection(
                    title = extractDocumentationTitle(content),
                    content = content,
                    url = docUrl,
                    type = determineDocumentationType(docUrl, content)
                ))
            } catch (e: Exception) {
                logger.debug("Failed to fetch documentation from $docUrl", e)
            }
        }
        
        return documentation
    }
    
    private fun discoverDocumentationUrls(apiUrl: String): List<String> {
        val baseUrl = extractBaseUrl(apiUrl)
        val commonDocPaths = listOf(
            "/docs", "/documentation", "/api-docs", "/dev-docs",
            "/swagger-ui", "/swagger-ui.html", "/redoc",
            "/openapi", "/api/docs", "/developer", "/reference"
        )
        
        return commonDocPaths.map { "$baseUrl$it" }
    }
    
    private fun detectSpecification(apiUrl: String): ApiSpecification {
        // Try common paths: /swagger.json, /v3/api-docs, /graphql
        val commonPaths = listOf(
            "/swagger.json", "/v2/api-docs", "/swagger.yaml",
            "/v3/api-docs", "/openapi.json", "/openapi.yaml",
            "/graphql", "/.well-known/openapi_description"
        )
        
        for (path in commonPaths) {
            try {
                val response = httpClient.get("$apiUrl$path")
                if (response.isSuccessful) {
                    return when {
                        path.contains("v3") || response.body?.contains("openapi") == true -> ApiSpecification.OPENAPI_3_0
                        path.contains("swagger") || path.contains("v2") -> ApiSpecification.OPENAPI_2_0
                        path.contains("graphql") -> ApiSpecification.GRAPHQL
                        else -> ApiSpecification.REST_DISCOVERY
                    }
                }
            } catch (e: Exception) {
                logger.debug("Failed to check $apiUrl$path", e)
            }
        }
        
        return ApiSpecification.REST_DISCOVERY
    }
}
```

#### 2. Credential Management Service

```kotlin
// meta-agent-core/src/main/kotlin/com/embabel/metaagent/discovery/CredentialManagementService.kt
@Service
class CredentialManagementService {
    
    data class CredentialRequirement(
        val apiName: String,
        val authType: AuthenticationScheme,
        val registrationUrl: String,
        val documentationUrl: String,
        val requiredScopes: List<String> = emptyList(),
        val testEndpoint: String,
        val setupInstructions: String
    )
    
    fun analyzeCredentialRequirements(apiAnalysis: ApiAnalysis): CredentialRequirement? {
        return when (apiAnalysis.authenticationScheme) {
            AuthenticationScheme.API_KEY -> CredentialRequirement(
                apiName = apiAnalysis.apiName,
                authType = AuthenticationScheme.API_KEY,
                registrationUrl = discoverRegistrationUrl(apiAnalysis.baseUrl),
                documentationUrl = discoverDocumentationUrl(apiAnalysis.baseUrl),
                testEndpoint = findTestEndpoint(apiAnalysis.endpoints),
                setupInstructions = generateApiKeyInstructions(apiAnalysis)
            )
            
            AuthenticationScheme.OAUTH2 -> CredentialRequirement(
                apiName = apiAnalysis.apiName,
                authType = AuthenticationScheme.OAUTH2,
                registrationUrl = discoverOAuthRegistrationUrl(apiAnalysis.baseUrl),
                documentationUrl = discoverDocumentationUrl(apiAnalysis.baseUrl),
                requiredScopes = extractOAuthScopes(apiAnalysis.endpoints),
                testEndpoint = findTestEndpoint(apiAnalysis.endpoints),
                setupInstructions = generateOAuthInstructions(apiAnalysis)
            )
            
            AuthenticationScheme.NONE -> null
            else -> throw UnsupportedAuthException("Unsupported auth scheme: ${apiAnalysis.authenticationScheme}")
        }
    }
    
    fun promptUserForCredentials(requirement: CredentialRequirement): InteractiveCredentialSetup {
        return InteractiveCredentialSetup(
            instruction = """
            🔐 API Key Required: ${requirement.apiName}
            
            To use ${requirement.apiName}, you need to register for an API key:
            
            1. Visit: ${requirement.registrationUrl}
            2. Create an account and generate an API key
            3. Documentation: ${requirement.documentationUrl}
            
            ${requirement.setupInstructions}
            
            Once you have your API key, we'll test the connection and continue.
            """.trimIndent(),
            
            testEndpoint = requirement.testEndpoint,
            validationStrategy = createValidationStrategy(requirement)
        )
    }
}
```

#### 3. Tool Testing Service

```kotlin
// meta-agent-core/src/main/kotlin/com/embabel/metaagent/discovery/ToolTestingService.kt
@Service
class ToolTestingService {
    
    data class ToolTestResult(
        val toolName: String,
        val testStatus: TestStatus,
        val responseTime: Duration,
        val errorMessage: String? = null,
        val sampleResponse: Any? = null,
        val testCoverage: TestCoverage
    )
    
    enum class TestStatus {
        PASSED, FAILED, AUTHENTICATION_FAILED, RATE_LIMITED, TIMEOUT
    }
    
    data class TestCoverage(
        val endpointsTested: Int,
        val totalEndpoints: Int,
        val authenticationTested: Boolean,
        val errorHandlingTested: Boolean,
        val rateLimitTested: Boolean
    )
    
    fun validateToolIntegration(
        apiAnalysis: ApiAnalysis,
        credentials: Map<String, String>
    ): ToolTestResult {
        val startTime = System.currentTimeMillis()
        
        try {
            // 1. Test authentication
            val authResult = testAuthentication(apiAnalysis, credentials)
            if (!authResult.success) {
                return ToolTestResult(
                    toolName = apiAnalysis.apiName,
                    testStatus = TestStatus.AUTHENTICATION_FAILED,
                    responseTime = Duration.ofMillis(System.currentTimeMillis() - startTime),
                    errorMessage = authResult.errorMessage,
                    testCoverage = TestCoverage(0, apiAnalysis.endpoints.size, false, false, false)
                )
            }
            
            // 2. Test core endpoints
            val endpointResults = testCoreEndpoints(apiAnalysis, credentials)
            
            // 3. Test error handling
            val errorHandlingResult = testErrorHandling(apiAnalysis, credentials)
            
            // 4. Test rate limiting behavior
            val rateLimitResult = testRateLimiting(apiAnalysis, credentials)
            
            val testCoverage = TestCoverage(
                endpointsTested = endpointResults.size,
                totalEndpoints = apiAnalysis.endpoints.size,
                authenticationTested = authResult.success,
                errorHandlingTested = errorHandlingResult.success,
                rateLimitTested = rateLimitResult.success
            )
            
            return ToolTestResult(
                toolName = apiAnalysis.apiName,
                testStatus = if (endpointResults.all { it.success }) TestStatus.PASSED else TestStatus.FAILED,
                responseTime = Duration.ofMillis(System.currentTimeMillis() - startTime),
                sampleResponse = endpointResults.firstOrNull()?.sampleResponse,
                testCoverage = testCoverage
            )
            
        } catch (e: Exception) {
            return ToolTestResult(
                toolName = apiAnalysis.apiName,
                testStatus = TestStatus.FAILED,
                responseTime = Duration.ofMillis(System.currentTimeMillis() - startTime),
                errorMessage = e.message,
                testCoverage = TestCoverage(0, apiAnalysis.endpoints.size, false, false, false)
            )
        }
    }
    
    fun generateTestSuite(apiAnalysis: ApiAnalysis): GeneratedTestSuite {
        return GeneratedTestSuite(
            testClass = generateTestClass(apiAnalysis),
            mockData = generateMockData(apiAnalysis),
            integrationTests = generateIntegrationTests(apiAnalysis),
            performanceTests = generatePerformanceTests(apiAnalysis)
        )
    }
}
```

#### 4. Environment Setup Service

```kotlin
// meta-agent-core/src/main/kotlin/com/embabel/metaagent/discovery/EnvironmentSetupService.kt
@Service
class EnvironmentSetupService {
    
    fun generateEnvironmentScripts(
        projectStructure: GeneratedAgentProjectStructure,
        toolRequirements: List<CredentialRequirement>
    ): EnvironmentSetup {
        
        val envVariables = toolRequirements.flatMap { requirement ->
            when (requirement.authType) {
                AuthenticationScheme.API_KEY -> listOf("${requirement.apiName.toUpperCase()}_API_KEY")
                AuthenticationScheme.OAUTH2 -> listOf(
                    "${requirement.apiName.toUpperCase()}_CLIENT_ID",
                    "${requirement.apiName.toUpperCase()}_CLIENT_SECRET"
                )
                else -> emptyList()
            }
        }
        
        return EnvironmentSetup(
            bashScript = generateBashScript(envVariables, toolRequirements),
            powershellScript = generatePowerShellScript(envVariables, toolRequirements),
            dockerCompose = generateDockerComposeWithSecrets(envVariables),
            applicationProperties = generateApplicationProperties(envVariables),
            readme = generateEnvironmentReadme(toolRequirements)
        )
    }
    
    private fun generateBashScript(
        envVars: List<String>,
        requirements: List<CredentialRequirement>
    ): String = """
        #!/bin/bash
        # Generated environment setup script for ${projectStructure.projectName}
        
        echo "🚀 Setting up environment for ${projectStructure.projectName}"
        
        # Export environment variables
        ${envVars.joinToString("\n") { "export $it=\"\${$it:-}\"" }}
        
        # Validate required environment variables
        ${envVars.joinToString("\n") { """
        if [ -z "$$it" ]; then
            echo "❌ Missing required environment variable: $it"
            echo "Please set $it in your environment"
            exit 1
        fi
        """.trimIndent() }}
        
        echo "✅ Environment setup complete"
        
        # Tool-specific setup instructions
        ${requirements.joinToString("\n\n") { requirement ->
            """
            echo "🔧 ${requirement.apiName} Setup:"
            echo "   Registration: ${requirement.registrationUrl}"
            echo "   Documentation: ${requirement.documentationUrl}"
            """.trimIndent()
        }}
    """.trimIndent()
}
```

#### 5. Tool Catalog Service

```kotlin
// meta-agent-core/src/main/kotlin/com/embabel/metaagent/discovery/ToolCatalogService.kt
@Service
class ToolCatalogService(
    private val modelProvider: ModelProvider,  // Configured via embabel-agent-api
    private val toolRegistry: ToolRegistry,
    private val apiIntrospectionService: ApiIntrospectionService
) {
    
    data class ToolDefinition(
        val name: String,                        // "OpenTable API"
        val description: String,                 // "Restaurant reservation system"
        val apiUrl: String,                      // "https://api.opentable.com"
        val toolGroup: String,                   // "EXTERNAL_API_1"
        val dependencies: List<String>,          // Maven dependencies
        val configuration: Map<String, Any>,     // Required config properties
        val apiType: ToolType,                   // REST_API, MCP_SERVER, JAVA_LIB
        val authRequired: Boolean = false,       // API key needed
        val costModel: CostModel? = null,        // Pricing information
        val apiAnalysis: ApiAnalysis? = null,    // Deep API introspection results
        val lastAnalyzed: Instant? = null        // When API was last analyzed
    )
    
    private val toolCatalog = mutableMapOf<String, ToolDefinition>()
    
    fun discoverTools(agentDescription: String): List<ToolRecommendation> {
        val intent = extractIntent(agentDescription)
        val candidates = findCandidateTools(intent)
        
        // Perform deep API introspection for each candidate
        val analyzedCandidates = candidates.map { tool ->
            val analysis = if (tool.apiAnalysis == null || isStaleAnalysis(tool.lastAnalyzed)) {
                performDeepAnalysis(tool)
            } else {
                tool.apiAnalysis
            }
            
            tool.copy(
                apiAnalysis = analysis,
                lastAnalyzed = Instant.now()
            )
        }
        
        return matchToolsWithCapabilities(analyzedCandidates, intent)
    }
    
    private fun extractIntent(description: String): AgentIntent {
        // ModelProvider configured via embabel-agent-api properties
        // Supports: OpenAI, Anthropic, Bedrock, Hugging Face, Ollama (local/docker)
        return modelProvider.defaultLlm().createObject(
            """
            Analyze this agent description and extract generic, domain-agnostic requirements:
            - Required HTTP operations (GET, POST, PUT, DELETE, etc.)
            - Data flow patterns (read-only, read-write, batch processing, etc.)
            - Integration complexity (simple REST calls vs complex workflows)
            - Performance requirements (real-time vs batch processing)
            - Authentication requirements (API keys, OAuth, etc.)
            
            Agent description: "$description"
            
            Return only generic, technical requirements without domain-specific assumptions.
            Focus on what the agent needs to DO, not what domain it operates in.
            """.trimIndent()
        )
    }
    
    private fun performDeepAnalysis(tool: ToolDefinition): ApiAnalysis {
        return try {
            logger.info("🔍 Performing deep API analysis for ${tool.name}")
            val analysis = apiIntrospectionService.analyzeApi(tool.apiUrl)
            
            // Cache the analysis results
            toolRegistry.updateToolAnalysis(tool.name, analysis)
            
            logger.info("✅ Analysis complete for ${tool.name}: ${analysis.endpoints.size} endpoints found")
            analysis
        } catch (e: Exception) {
            logger.error("❌ Failed to analyze API for ${tool.name}", e)
            createFallbackAnalysis(tool)
        }
    }
    
    private fun matchToolsWithCapabilities(
        analyzedTools: List<ToolDefinition>, 
        intent: AgentIntent
    ): List<ToolRecommendation> {
        
        return analyzedTools.mapNotNull { tool ->
            val analysis = tool.apiAnalysis ?: return@mapNotNull null
            
            // Match specific endpoints to required capabilities
            val relevantEndpoints = analysis.endpoints.filter { endpoint ->
                isEndpointRelevant(endpoint, intent)
            }
            
            if (relevantEndpoints.isEmpty()) {
                logger.debug("No relevant endpoints found for ${tool.name}")
                return@mapNotNull null
            }
            
            // Calculate match score based on endpoint capabilities
            val matchScore = calculateEndpointMatchScore(relevantEndpoints, intent)
            
            ToolRecommendation(
                tool = tool,
                matchScore = matchScore,
                relevantEndpoints = relevantEndpoints,
                integrationStrategy = determineIntegrationStrategy(relevantEndpoints, intent),
                requiredCredentials = analysis.authenticationScheme != AuthenticationScheme.NONE,
                estimatedComplexity = assessIntegrationComplexity(relevantEndpoints),
                usageExamples = generateUsageExamples(relevantEndpoints, intent)
            )
        }.sortedByDescending { it.matchScore }
    }
    
    private fun isEndpointRelevant(endpoint: EndpointDefinition, intent: AgentIntent): Boolean {
        // Pure semantic matching - no domain knowledge
        val endpointText = "${endpoint.path} ${endpoint.summary} ${endpoint.description}".lowercase()
        
        // Check if endpoint description matches intent requirements via LLM
        // ModelProvider configured via embabel-agent-api (OpenAI, Anthropic, Bedrock, etc.)
        val semanticMatch = modelProvider.defaultLlm().createObject(
            """
            Determine if this API endpoint is relevant for the given agent intent:
            
            Endpoint: ${endpoint.method} ${endpoint.path}
            Summary: ${endpoint.summary}
            Description: ${endpoint.description}
            
            Agent Intent: ${intent.description}
            Required Operations: ${intent.requiredOperations}
            
            Return true if the endpoint could be useful for this agent, false otherwise.
            Consider only the functional match, not domain-specific knowledge.
            """.trimIndent()
        )
        
        // Fallback to HTTP operation matching
        val httpOperationMatch = intent.requiredOperations.any { operation ->
            endpoint.httpOperation.toString().lowercase().contains(operation.lowercase())
        }
        
        return semanticMatch || httpOperationMatch
    }
    
    private fun calculateEndpointMatchScore(
        endpoints: List<EndpointDefinition>,
        intent: AgentIntent
    ): Double {
        var score = 0.0
        
        // Score based on HTTP operation alignment
        score += endpoints.count { endpoint ->
            intent.requiredOperations.any { operation ->
                endpoint.method.equals(operation, ignoreCase = true)
            }
        } * 10.0
        
        // Score based on semantic relevance (via LLM)
        score += endpoints.count { endpoint ->
            isEndpointRelevant(endpoint, intent)
        } * 8.0
        
        // Score based on endpoint completeness (has documentation, examples)
        score += endpoints.count { endpoint ->
            endpoint.description?.isNotBlank() == true
        } * 2.0
        
        // Score based on authentication match
        score += endpoints.count { endpoint ->
            // This would be determined from the API analysis
            true // Placeholder - actual auth matching logic
        } * 1.0
        
        return score / endpoints.size.coerceAtLeast(1)
    }
}

data class AgentIntent(
    val description: String,                     // Original agent description
    val requiredOperations: List<String>,        // ["GET", "POST", "PUT"]
    val dataFlowPatterns: List<String>,          // ["read_only", "read_write", "batch"]
    val integrationComplexity: String,          // "simple" | "complex" | "workflow"
    val performanceRequirements: String,        // "real_time" | "batch" | "standard"
    val authenticationNeeds: List<String>       // ["api_key", "oauth", "none"]
)

data class ToolRecommendation(
    val tool: ToolDefinition,
    val confidence: Double,                      // 0.0-1.0 match confidence
    val reasoning: String,                       // Why this tool was recommended
    val requiredConfig: Map<String, Any>,        // Config needed for this agent
)
```

#### 2. LLM-Powered Semantic Matcher

```kotlin
// meta-agent-core/src/main/kotlin/com/embabel/metaagent/discovery/SemanticMatcher.kt
@Service
class SemanticMatcher(
    private val modelProvider: ModelProvider,
    private val toolCatalog: ToolCatalogService,
) {
    
    fun findBestTools(
        agentDescription: String,
        maxTools: Int = 5
    ): List<ToolRecommendation> {
        
        val analysisPrompt = """
        I need to find the best APIs/tools for this agent:
        "$agentDescription"
        
        Available tools:
        ${toolCatalog.getAllTools().joinToString("\n") { tool ->
            "- ${tool.name}: ${tool.description} (${tool.capabilities.joinToString()})"
        }}
        
        Select the most relevant tools and explain why each is needed.
        Consider:
        - Direct capability match (booking -> OpenTable)
        - Supporting capabilities (location -> HERE Maps)
        - Data dependencies (reviews -> Yelp)
        - Integration patterns (payments -> Stripe)
        
        Return top $maxTools tools with confidence scores.
        """.trimIndent()
        
        return modelProvider.defaultLlm().createObject<List<ToolRecommendation>>(
            analysisPrompt
        )
    }
    
    fun validateToolCombination(
        tools: List<ToolRecommendation>,
        agentDescription: String
    ): ValidationResult {
        val validationPrompt = """
        Validate if these tools work well together for: "$agentDescription"
        
        Selected tools:
        ${tools.joinToString("\n") { "- ${it.tool.name}: ${it.reasoning}" }}
        
        Check for:
        - Missing critical capabilities
        - Redundant/conflicting tools
        - Integration complexity
        - Cost considerations
        
        Suggest improvements if needed.
        """.trimIndent()
        
        return modelProvider.defaultLlm().createObject(validationPrompt)
    }
}
```

#### 3. Auto-Configuration Generator

```kotlin
// meta-agent-core/src/main/kotlin/com/embabel/metaagent/generation/AutoConfigGenerator.kt
@Service
class AutoConfigGenerator {
    
    fun generateConfiguration(
        agentContext: MetaAgentContext,
        discoveredTools: List<ToolRecommendation>
    ): GeneratedConfiguration {
        
        val mavenDependencies = generateMavenDependencies(discoveredTools)
        val springConfiguration = generateSpringConfig(discoveredTools)
        val toolGroupConfig = generateToolGroupConfig(discoveredTools)
        val applicationProperties = generateApplicationProperties(discoveredTools)
        
        return GeneratedConfiguration(
            mavenDependencies = mavenDependencies,
            springConfiguration = springConfiguration,
            toolGroupConfig = toolGroupConfig,
            applicationProperties = applicationProperties,
            environmentVariables = generateEnvVars(discoveredTools)
        )
    }
    
    private fun generateMavenDependencies(tools: List<ToolRecommendation>): String {
        return tools.flatMap { it.tool.dependencies }.distinct().joinToString("\n") { dep ->
            """
            <dependency>
                <groupId>${dep.substringBefore(":")}</groupId>
                <artifactId>${dep.substringAfter(":").substringBefore(":")}</artifactId>
                <version>${dep.substringAfterLast(":")}</version>
            </dependency>
            """.trimIndent()
        }
    }
    
    private fun generateToolGroupConfig(tools: List<ToolRecommendation>): String {
        return tools.joinToString("\n\n") { tool ->
            """
            @Bean
            fun ${tool.tool.name.lowercase().replace(" ", "")}ToolGroup(): ToolGroup {
                return CustomToolGroup(
                    name = "${tool.tool.toolGroup}",
                    description = "${tool.tool.description}",
                    capabilities = listOf(${tool.tool.capabilities.joinToString { "\"$it\"" }})
                )
            }
            """.trimIndent()
        }
    }
}
```

### Real-World Examples

#### Restaurant Agent Discovery

**User Input:**
```bash
meta-agent:> generate "restaurant booking agent for Berlin" --language kotlin --test-framework kotest --doc-type markdown
```

**Autodiscovery Process:**
```kotlin
// 1. Intent Analysis
val intent = AgentIntent(
    domain = "restaurant",
    capabilities = ["booking", "search", "availability"],
    dataNeeds = ["location", "cuisine", "pricing", "reviews"],
    integrationNeeds = ["external_api", "real_time"]
)

// 2. Tool Matching
val discoveredTools = listOf(
    ToolRecommendation(
        tool = openTableTool,
        confidence = 0.95,
        reasoning = "Direct match: restaurant booking capability"
    ),
    ToolRecommendation(
        tool = hereMapsToolGroup,
        confidence = 0.85,
        reasoning = "Location services needed for Berlin restaurants"
    ),
    ToolRecommendation(
        tool = yelpTool,
        confidence = 0.75,
        reasoning = "Restaurant reviews and additional data"
    )
)

// 3. Auto-Generated Configuration
val config = GeneratedConfiguration(
    mavenDependencies = """
        <dependency>
            <groupId>com.opentable</groupId>
            <artifactId>opentable-api</artifactId>
            <version>2.1.0</version>
        </dependency>
        <dependency>
            <groupId>com.here</groupId>
            <artifactId>here-maps-api</artifactId>
            <version>1.4.0</version>
        </dependency>
    """,
    applicationProperties = """
        opentable.api.key=${OPENTABLE_API_KEY}
        here.api.key=${HERE_API_KEY}
        restaurant.search.radius=5km
        restaurant.booking.timeout=30s
    """
)
```

**Generated Agent with Auto-Discovered Tools:**
```kotlin
// Auto-generated with discovered tools
@Agent(description = "Restaurant booking agent for Berlin with auto-discovered tools")
class RestaurantBookingAgent(
    private val openTableService: OpenTableService,     // Auto-discovered
    private val hereMapsService: HereMapsService,        // Auto-discovered
    private val yelpService: YelpService,                // Auto-discovered
    @Value("\${restaurant.model:gpt-4.1-mini}") private val model: String,
) {
    
    @Action(toolGroups = [
        "RESTAURANT_BOOKING",    // Auto-discovered OpenTable
        "LOCATION_SERVICES",     // Auto-discovered HERE Maps
        "RESTAURANT_REVIEWS"     // Auto-discovered Yelp
    ])
    fun findAndBookRestaurant(
        preferences: DiningPreferences,
        location: String = "Berlin"
    ): BookingResult =
        usingModel(model).createObject(
            """
            Find and book a restaurant in $location matching:
            ${preferences.cuisine}, ${preferences.priceRange}
            
            Use OpenTable for booking, HERE Maps for location, Yelp for reviews.
            """.trimIndent()
        )
}
```

#### Travel Coordinator Discovery

**User Input:**
```bash
meta-agent:> generate "travel planning agent with hotels and flights" --language kotlin --build-tool gradle --spring-version 3.2.0
```

**Auto-Discovered Tools:**
- **Amadeus API** (flights) - confidence: 0.92
- **Booking.com API** (hotels) - confidence: 0.90  
- **HERE Maps** (locations) - confidence: 0.88
- **Weather API** (travel conditions) - confidence: 0.70

**Generated Configuration:**
```xml
<!-- Auto-generated Maven dependencies -->
<dependency>
    <groupId>com.amadeus</groupId>
    <artifactId>amadeus-java</artifactId>
    <version>5.2.0</version>
</dependency>
<dependency>
    <groupId>com.booking</groupId>
    <artifactId>booking-api</artifactId>
    <version>1.8.0</version>
</dependency>
```

#### Payment Processing Discovery

**User Input:**
```bash
meta-agent:> generate "secure payment processing agent" --language java --test-framework junit5 --java-version 21
```

**Auto-Discovered Tools:**
- **Stripe API** (payments) - confidence: 0.95
- **Fraud Detection API** (security) - confidence: 0.85
- **Tax Calculator API** (calculations) - confidence: 0.60

### Tool Catalog Management

#### Built-in Tool Definitions

```kotlin
// Pre-configured tool catalog with deep API introspection
// Note: These are examples showing different API types and domains
// The system can analyze and integrate ANY well-documented API
val exampleApiTools = listOf(
    ToolDefinition(
        name = "OpenTable API",
        description = "Restaurant reservation and booking system with real-time availability",
        apiUrl = "https://api.opentable.com",
        toolGroup = "EXTERNAL_API_1",
        dependencies = listOf("com.opentable:opentable-api:2.1.0"),
        configuration = mapOf(
            "base_url" to "https://api.opentable.com",
            "timeout" to 30000,
            "retry_attempts" to 3
        ),
        apiType = ToolType.REST_API,
        authRequired = true,
        costModel = CostModel(
            requestCost = 0.001, // $0.001 per request
            rateLimits = mapOf("requests_per_minute" to 1000)
        )
    ),
    ToolDefinition(
        name = "HERE Maps",
        description = "Location services, mapping, and geocoding with routing capabilities",
        apiUrl = "https://api.here.com",
        toolGroup = "EXTERNAL_API_2",
        dependencies = listOf("com.here:here-maps-api:1.4.0"),
        configuration = mapOf(
            "base_url" to "https://api.here.com",
            "geocoder_url" to "https://geocoding.api.here.com",
            "routing_url" to "https://routing.api.here.com"
        ),
        apiType = ToolType.REST_API,
        authRequired = true,
        costModel = CostModel(
            requestCost = 0.0005, // $0.0005 per request
            rateLimits = mapOf("requests_per_second" to 100)
        )
    ),
    ToolDefinition(
        name = "Resy API",
        description = "Alternative restaurant reservation platform with premium dining focus",
        apiUrl = "https://api.resy.com",
        toolGroup = "EXTERNAL_API_3",
        dependencies = listOf("com.resy:resy-api:1.0.0"),
        configuration = mapOf(
            "base_url" to "https://api.resy.com",
            "api_version" to "v3"
        ),
        apiType = ToolType.REST_API,
        authRequired = true
    )
)

    ToolDefinition(
        name = "Stripe API",
        description = "Comprehensive payment processing with subscriptions and marketplace support",
        apiUrl = "https://api.stripe.com",
        toolGroup = "EXTERNAL_API_4",
        dependencies = listOf("com.stripe:stripe-java:20.0.0"),
        configuration = mapOf(
            "base_url" to "https://api.stripe.com",
            "api_version" to "2023-10-16",
            "connect_base_url" to "https://connect.stripe.com"
        ),
        apiType = ToolType.REST_API,
        authRequired = true,
        costModel = CostModel(
            requestCost = 0.0,
            transactionFee = 0.029, // 2.9% + $0.30 per transaction
            rateLimits = mapOf("requests_per_second" to 100)
        )
    ),
    ToolDefinition(
        name = "PayPal API",
        description = "Global payment platform with digital wallet and merchant services",
        apiUrl = "https://api.paypal.com",
        toolGroup = "EXTERNAL_API_5",
        dependencies = listOf("com.paypal:paypal-java-sdk:1.14.0"),
        configuration = mapOf(
            "base_url" to "https://api.paypal.com",
            "sandbox_url" to "https://api.sandbox.paypal.com",
            "api_version" to "v2"
        ),
        apiType = ToolType.REST_API,
        authRequired = true
    ),
    ToolDefinition(
        name = "Amadeus API",
        description = "Flight search, booking, and travel content with real-time data",
        apiUrl = "https://api.amadeus.com",
        toolGroup = "EXTERNAL_API_6",
        dependencies = listOf("com.amadeus:amadeus-java:5.0.0"),
        configuration = mapOf(
            "base_url" to "https://api.amadeus.com",
            "auth_url" to "https://api.amadeus.com/v1/security/oauth2/token"
        ),
        apiType = ToolType.REST_API,
        authRequired = true,
        costModel = CostModel(
            requestCost = 0.01, // $0.01 per search request
            rateLimits = mapOf("requests_per_second" to 10)
        )
    )
)

// Auto-register example tools with deep analysis on startup
// In production, tools would be discovered dynamically or configured per deployment
@PostConstruct
fun initializeToolCatalog() {
    val allExampleTools = exampleApiTools
    
    allExampleTools.forEach { tool ->
        logger.info("🔍 Initializing deep analysis for ${tool.name}")
        
        // Perform API introspection asynchronously
        CompletableFuture.runAsync {
            try {
                val analysis = apiIntrospectionService.analyzeApi(tool.apiUrl)
                val enrichedTool = tool.copy(
                    apiAnalysis = analysis,
                    lastAnalyzed = Instant.now()
                )
                toolCatalog[tool.name] = enrichedTool
                
                logger.info("✅ ${tool.name} analyzed: ${analysis.endpoints.size} endpoints, " +
                           "${analysis.endpointCapabilities.size} capabilities")
            } catch (e: Exception) {
                logger.warn("⚠️ Failed to analyze ${tool.name}: ${e.message}")
                toolCatalog[tool.name] = tool // Store without analysis
            }
        }
    }
}
```

#### Dynamic Tool Registration

```kotlin
// Runtime tool discovery from MCP servers
@Service
class McpToolDiscoveryService(
    private val mcpClients: List<McpSyncClient>
) {
    
    fun discoverMcpTools(): List<ToolDefinition> {
        return mcpClients.flatMap { client ->
            client.listTools().map { mcpTool ->
                ToolDefinition(
                    name = mcpTool.name,
                    description = mcpTool.description,
                    capabilities = extractCapabilities(mcpTool),
                    domains = extractDomains(mcpTool),
                    toolGroup = "MCP_${mcpTool.name.uppercase()}",
                    dependencies = emptyList(), // MCP managed
                    apiType = ToolType.MCP_SERVER
                )
            }
        }
    }
}
```

### Integration with Meta-Agent Generation

#### Enhanced MetaAgent with Autodiscovery

```kotlin
@Component
class MetaAgent(
    private val modelProvider: ModelProvider,
    private val templateEngine: TemplateEngine,
    private val toolDiscoveryService: ToolCatalogService,    // New!
    private val semanticMatcher: SemanticMatcher,            // New!
    private val autoConfigGenerator: AutoConfigGenerator,    // New!
    private val validator: KotlinValidator,
) {
    
    fun generateAgent(context: MetaAgentContext): GeneratedAgentModel {
        // 1. SMART DEFAULT: If user provided tools, use them (no auto-discovery)
        val discoveredTools = if (context.toolGroups.isNotEmpty()) {
            logger.info("Using user-provided tools: ${context.toolGroups}")
            context.toolGroups.map { findToolByGroup(it) }
        } else {
            // User provided NO tools - try auto-discovery if enabled
            logger.info("No tools provided - attempting auto-discovery")
            toolDiscoveryService.discoverTools(context.task)
        }
        
        // 2. Generate configuration for discovered tools
        val autoConfig = autoConfigGenerator.generateConfiguration(context, discoveredTools)
        
        // 3. Enhanced prompt with tool context
        val prompt = buildEnhancedPrompt(context, discoveredTools)
        
        // 4. Generate agent with auto-discovered tools
        val llm = modelProvider.defaultLlm()
        val agentSpec = llm.createObject<AgentSpecification>(prompt)
        
        // 5. Render with auto-configuration
        val sourceCode = templateEngine.renderAgentWithTools(
            agentSpec, 
            context.language, 
            discoveredTools
        )
        
        // 6. Validate and return complete project
        validator.validate(sourceCode)
        return GeneratedAgentModel(
            sourceCode = sourceCode,
            projectStructure = buildGeneratedAgentProjectStructure(context, autoConfig),
            discoveredTools = discoveredTools,
            autoConfiguration = autoConfig
        )
    }
    
    private fun buildEnhancedPrompt(
        context: MetaAgentContext, 
        tools: List<ToolRecommendation>
    ): String = """
        Generate a ${context.language} agent that ${context.task}.
        
        Available tools (auto-discovered):
        ${tools.joinToString("\n") { tool ->
            "- ${tool.tool.name}: ${tool.reasoning}"
        }}
        
        Use these tools in appropriate @Action methods with proper toolGroups.
        Include proper error handling and configuration.
        Generate complete Spring Boot agent with Embabel annotations.
        """.trimIndent()
}
```

This architecture makes meta-agent truly intelligent - users describe intent, the system automatically discovers and integrates the right tools, generating complete, production-ready agents.

---

## 🎯 Smart Defaults: User-Provided vs Auto-Discovery

### Intelligent Tool Selection Logic

```kotlin
// meta-agent-core/src/main/kotlin/com/embabel/metaagent/MetaAgent.kt
@Component
class MetaAgent(
    private val modelProvider: ModelProvider,
    private val templateEngine: TemplateEngine,
    private val toolDiscoveryService: Optional<ToolDiscoveryService>,
    private val properties: MetaAgentProperties,
    private val validator: KotlinValidator,
) {
    
    fun generateAgent(context: MetaAgentContext): GeneratedAgentModel {
        val finalTools = selectTools(context)
        
        val prompt = buildPrompt(context, finalTools)
        val llm = modelProvider.defaultLlm()
        val agentSpec = llm.createObject<AgentSpecification>(prompt)
        
        val sourceCode = templateEngine.renderAgent(agentSpec, context.language)
        validator.validate(sourceCode)
        
        return GeneratedAgentModel(
            sourceCode = sourceCode,
            toolSelectionMethod = getSelectionMethod(context, finalTools),
            discoveredTools = finalTools
        )
    }
    
    private fun selectTools(context: MetaAgentContext): List<String> {
        return when {
            // Case 1: User explicitly provided tools - use them (highest priority)
            context.toolGroups.isNotEmpty() -> {
                logger.info("✅ Using user-provided tools: ${context.toolGroups}")
                context.toolGroups
            }
            
            // Case 2: No tools provided + auto-discovery enabled - discover tools
            properties.toolDiscovery.enabled && toolDiscoveryService.isPresent -> {
                logger.info("🔍 No tools provided - attempting auto-discovery")
                try {
                    val discovered = toolDiscoveryService.get().discoverTools(context.task)
                    val tools = discovered.map { it.tool.toolGroup }
                    logger.info("✅ Auto-discovered tools: $tools")
                    tools
                } catch (e: Exception) {
                    logger.warn("❌ Auto-discovery failed, generating without tools", e)
                    emptyList()
                }
            }
            
            // Case 3: No tools provided + auto-discovery disabled - generate without tools
            else -> {
                logger.info("⚠️  No tools provided, auto-discovery disabled - generating basic agent")
                emptyList()
            }
        }
    }
    
    private fun getSelectionMethod(context: MetaAgentContext, finalTools: List<String>): String {
        return when {
            context.toolGroups.isNotEmpty() -> "user_provided"
            finalTools.isNotEmpty() -> "auto_discovered"
            else -> "none"
        }
    }
}
```

### Real-World Usage Examples

#### Example 1: Expert User (Manual Tools)
```bash
# User knows exactly what they want
meta-agent:> generate "restaurant booking agent" --tools WEB,MAPS,RESTAURANT_BOOKING --language kotlin --test-framework kotest

✅ Using user-provided tools: [WEB, MAPS, RESTAURANT_BOOKING]
✅ Generated RestaurantAgent.kt with specified tools and Kotest testing framework
```

#### Example 2: Casual User (Auto-Discovery)
```bash
# User just describes what they want
meta-agent:> generate "restaurant booking agent" --language kotlin --doc-type asciidoc

🔍 No tools provided - attempting auto-discovery
✅ Auto-discovered tools: [RESTAURANT_BOOKING, LOCATION_SERVICES, RESTAURANT_REVIEWS]
✅ Generated RestaurantAgent.kt with auto-discovered tools and AsciiDoc documentation
```

#### Example 3: Conservative Setup (No Auto-Discovery)
```bash
# Auto-discovery disabled in config
meta-agent:> generate "restaurant booking agent" --language java --build-tool gradle --java-version 17

⚠️  No tools provided, auto-discovery disabled - generating basic agent
✅ Generated basic RestaurantAgent.java with Gradle build and Java 17 (user can add tools later)
```

### Configuration Examples

#### Smart Defaults Configuration
```yaml
# application.yml - Recommended setup
meta-agent:
  tool-discovery:
    enabled: true                    # Enable auto-discovery
    mode: BASIC                      # Start with simple keyword matching
    fallback-to-manual: true         # Safe fallback if discovery fails
    max-tools: 5                     # Reasonable limit
    confidence-threshold: 0.7        # Conservative threshold
```

#### Conservative Configuration
```yaml
# application-prod.yml - Production-safe setup
meta-agent:
  tool-discovery:
    enabled: false                   # Disable auto-discovery for stability
    # Users must provide tools manually
```

#### Developer Configuration
```yaml
# application-dev.yml - Development setup
meta-agent:
  tool-discovery:
    enabled: true
    mode: ADVANCED                   # Use LLM for semantic matching
    max-tools: 10                    # Allow more tools
    confidence-threshold: 0.5        # More permissive
    providers: ["builtin", "mcp"]    # Include MCP server tools
```

### Shell Command Behavior

```kotlin
@ShellMethod("Generate agent with smart tool selection")
fun generate(
    @ShellOption(help = "Agent description") task: String,
    @ShellOption(value = ["--tools", "-t"], help = "Manual tools (optional)") tools: List<String> = emptyList(),
    @ShellOption(value = ["--language", "-l"]) language: String = "kotlin",
    @ShellOption(value = ["--test-framework", "-tf"]) testFramework: String = "default",
    @ShellOption(value = ["--doc-type", "-dt"]) docType: String = "markdown",
    @ShellOption(value = ["--build-tool", "-bt"]) buildTool: String = "maven"
): String {
    val targetLang = TargetLanguage.fromString(language)
    val context = MetaAgentContext(
        userInput = UserInput(task),
        targetLanguage = targetLang,
        generationSettings = GenerationSettings(
            testFramework = if (testFramework == "default") 
                TestFramework.forLanguage(targetLang) 
            else when(testFramework.lowercase()) {
                "junit5" -> TestFramework.JUNIT5
                "junit4" -> TestFramework.JUNIT4
                "testng" -> TestFramework.TESTNG
                "spock" -> TestFramework.SPOCK
                "kotest" -> TestFramework.KOTEST
                else -> TestFramework.forLanguage(targetLang)
            },
            documentationType = DocumentationType.fromString(docType),
            buildTool = BuildTool.fromString(buildTool)
        )
    )
    
    val result = metaAgent.generateAgent(context)
    
    return buildResponse(result)
}

private fun buildResponse(result: GeneratedAgentModel): String {
    val methodDescription = when (result.toolSelectionMethod) {
        "user_provided" -> "✅ Generated with your specified tools"
        "auto_discovered" -> "🔍 Auto-discovered and integrated tools"
        "none" -> "⚠️  Generated without tools (you can add them later)"
        else -> "✅ Generated agent"
    }
    
    return """
    $methodDescription
    
    Tools used: ${result.discoveredTools.joinToString(", ")}
    
    ${result.sourceCode}
    """.trimIndent()
}
```

This approach provides **maximum flexibility** while maintaining **intelligent defaults** - exactly what users expect from modern tooling!

### Advanced Shell Command Examples with Strongly-Typed Enums

#### Java Enterprise Agent with Custom Configuration
```bash
meta-agent:> generate "enterprise inventory management system" \
  --language java \
  --test-framework junit5 \
  --doc-type asciidoc \
  --build-tool maven \
  --spring-version 3.2.0 \
  --java-version 21 \
  --tools WEB,DATABASE,SECURITY

✅ Generated Java enterprise agent with:
   - Target Language: Java 21 LTS
   - Test Framework: JUnit 5
   - Documentation: AsciiDoc
   - Build Tool: Apache Maven
   - Spring Boot: 3.2.0
   - Tools: [WEB, DATABASE, SECURITY]
```

#### Kotlin Microservice with Gradle
```bash
meta-agent:> generate "reactive microservice for user notifications" \
  --language kotlin \
  --test-framework kotest \
  --doc-type markdown \
  --build-tool gradle \
  --spring-version 3.1.0 \
  --kotlin-version 2.1.0

✅ Generated Kotlin microservice with:
   - Target Language: Kotlin 2.1.0
   - Test Framework: Kotest (language-optimized)
   - Documentation: Markdown
   - Build Tool: Gradle
   - Spring Boot: 3.1.0
```

#### Scala Agent with Spock Testing
```bash
meta-agent:> generate "financial analytics processor" \
  --language scala \
  --test-framework spock \
  --doc-type html \
  --build-tool gradle \
  --java-version 17

✅ Generated Scala agent with:
   - Target Language: Scala
   - Test Framework: Spock
   - Documentation: HTML
   - Build Tool: Gradle
   - Java Version: 17 LTS
```

#### Legacy Java Support
```bash
meta-agent:> generate "legacy system integration bridge" \
  --language java \
  --test-framework junit4 \
  --spring-version 3.0.0 \
  --java-version 11 \
  --build-tool maven

✅ Generated legacy-compatible agent with:
   - Target Language: Java 11 LTS
   - Test Framework: JUnit 4 (legacy compatibility)
   - Spring Boot: 3.0.0
   - Build Tool: Apache Maven
```

#### Full Configuration Showcase
```bash
meta-agent:> generate "AI-powered customer service bot" \
  --language kotlin \
  --test-framework kotest \
  --doc-type asciidoc \
  --build-tool gradle \
  --spring-version 3.2.0 \
  --kotlin-version 2.1.0 \
  --java-version 21 \
  --tools WEB,AI,DATABASE,MESSAGING

✅ Generated comprehensive agent with all configurations:
   - Target Language: Kotlin 2.1.0 (on Java 21 LTS)
   - Test Framework: Kotest
   - Documentation: AsciiDoc  
   - Build Tool: Gradle
   - Spring Boot: 3.2.0
   - Tools: [WEB, AI, DATABASE, MESSAGING]
```

### Generation Settings Validation

The system validates all enum parameters and provides helpful error messages:

```bash
meta-agent:> generate "test agent" --language python

❌ Error: Unsupported language: python. Supported: kotlin, java, scala
💡 Suggestion: Use --language kotlin for modern JVM development

meta-agent:> generate "test agent" --test-framework cucumber

❌ Error: Unsupported test framework: cucumber
💡 Available: junit5, junit4, testng, spock, kotest
💡 Suggestion: Use --test-framework kotest for Kotlin or --test-framework junit5 for Java

meta-agent:> generate "test agent" --spring-version 2.7.0

❌ Error: Unsupported Spring Boot version: 2.7.0
💡 Supported versions: 3.2.0, 3.1.0, 3.0.0
💡 Suggestion: Use --spring-version 3.2.0 for latest features
```

---

## 🔧 Development Pipeline for Generated Agents

### Generated Agent Lifecycle

```
Agent Generation → Workspace Creation → Compile → Test → Package → Deploy
```

### **Option A: Local Development (.m2 Repository)**
Direct deployment to local Maven repository for immediate use in other projects.

### **Option B: Workspace Development** 
Structured workspace for development, iteration, and testing of generated agents.

### Workspace Structure

```
~/.meta-agent/workspace/
├── generated-agents/                    # All generated agent projects
│   ├── restaurant-agent-20241216-001/   # Timestamped generation
│   │   ├── src/main/kotlin/
│   │   │   └── com/generated/agent/
│   │   │       ├── RestaurantAgent.kt        # Generated agent class
│   │   │       ├── model/
│   │   │       │   ├── DiningPreferences.kt  # Generated data classes
│   │   │       │   └── BookingResult.kt
│   │   │       └── RestaurantAgentApplication.kt # Spring Boot main
│   │   ├── src/test/kotlin/
│   │   │   └── com/generated/agent/
│   │   │       └── RestaurantAgentTest.kt    # Generated tests
│   │   ├── src/main/resources/
│   │   │   ├── application.yml               # Generated configuration
│   │   │   └── application-test.yml
│   │   ├── pom.xml                          # Complete Maven project
│   │   └── target/                          # Build output
│   │       ├── classes/
│   │       ├── test-classes/
│   │       └── restaurant-agent-1.0.0-SNAPSHOT.jar
│   └── travel-agent-20241216-002/          # Another generated agent
└── meta-agent-service/                     # Meta-agent itself
```

### Pipeline Implementation

```kotlin
// meta-agent-core/src/main/kotlin/com/embabel/metaagent/pipeline/GeneratedAgentPipeline.kt
@Service
class GeneratedAgentPipeline(
    private val properties: MetaAgentProperties,
    private val mavenRunner: MavenRunner,
) {
    
    fun processGeneratedAgent(generatedAgent: GeneratedAgentModel, context: MetaAgentContext): PipelineResult {
        val workspaceDir = createWorkspaceDirectory(context)
        
        return try {
            // 1. Write generated code to workspace
            val projectDir = writeToWorkspace(generatedAgent, workspaceDir)
            
            // 2. Compile generated code
            val compileResult = compileAgent(projectDir)
            
            // 3. Run generated tests
            val testResult = runTests(projectDir)
            
            // 4. Package agent (optional)
            val packageResult = packageAgent(projectDir)
            
            // 5. Deploy to local repository (optional)
            val deployResult = deployToLocal(projectDir)
            
            PipelineResult(
                projectDir = projectDir,
                compileResult = compileResult,
                testResult = testResult,
                packageResult = packageResult,
                deployResult = deployResult
            )
        } catch (e: Exception) {
            PipelineResult.failure(e)
        }
    }
    
    private fun createWorkspaceDirectory(context: MetaAgentContext): Path {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        val agentName = context.task.replace(Regex("[^a-zA-Z0-9]"), "-").lowercase()
        val dirName = "$agentName-$timestamp"
        
        return properties.workspace.baseDirectory.resolve("generated-agents").resolve(dirName)
    }
    
    private fun writeToWorkspace(agent: GeneratedAgentModel, workspaceDir: Path): Path {
        val projectDir = workspaceDir.resolve(agent.projectName)
        
        // Create Maven project structure
        createDirectoryStructure(projectDir)
        
        // Write source code
        writeSourceCode(agent, projectDir)
        
        // Write tests
        writeTestCode(agent, projectDir)
        
        // Write configuration
        writeConfiguration(agent, projectDir)
        
        return projectDir
    }
    
    private fun compileAgent(projectDir: Path): CompileResult {
        return mavenRunner.execute(projectDir, listOf("clean", "compile"))
    }
    
    private fun runTests(projectDir: Path): TestResult {
        return mavenRunner.execute(projectDir, listOf("test"))
    }
    
    private fun packageAgent(projectDir: Path): PackageResult {
        return mavenRunner.execute(projectDir, listOf("package"))
    }
    
    private fun deployToLocal(projectDir: Path): DeployResult {
        return mavenRunner.execute(projectDir, listOf("install"))
    }
}
```

### Enhanced Shell Commands with Pipeline

```kotlin
// meta-agent-service/src/main/kotlin/MetaAgentCommands.kt
@ShellComponent
class MetaAgentCommands(
    private val metaAgent: MetaAgent,
    private val pipeline: GeneratedAgentPipeline,
) {
    
    @ShellMethod("Generate and test agent", key = ["generate", "gen"])
    fun generateAndTest(
        @ShellOption(help = "Agent description") task: String,
        @ShellOption(value = ["-t", "--tools"]) tools: List<String> = emptyList(),
        @ShellOption(value = ["-l", "--lang"]) language: String = "kotlin",
        @ShellOption(value = ["--test"], help = "Run tests after generation") runTests: Boolean = true,
        @ShellOption(value = ["--deploy"], help = "Deploy to local Maven repo") deploy: Boolean = false,
    ): String {
        
        // 1. Generate agent
        val context = MetaAgentContext(task, language, tools)
        val generatedAgent = metaAgent.generateAgent(context)
        
        // 2. Process through pipeline
        val pipelineResult = pipeline.processGeneratedAgent(generatedAgent, context)
        
        // 3. Return comprehensive result
        return buildPipelineReport(pipelineResult)
    }
    
    @ShellMethod("Test existing generated agent")
    fun testAgent(
        @ShellOption(help = "Agent project directory") projectDir: String
    ): String {
        val result = pipeline.runTests(Paths.get(projectDir))
        return formatTestResult(result)
    }
    
    @ShellMethod("Deploy generated agent to local repository")
    fun deployAgent(
        @ShellOption(help = "Agent project directory") projectDir: String
    ): String {
        val result = pipeline.deployToLocal(Paths.get(projectDir))
        return formatDeployResult(result)
    }
    
    private fun buildPipelineReport(result: PipelineResult): String {
        return """
        🎯 Agent Generation Pipeline Report
        
        📁 Project: ${result.projectDir}
        
        🔨 Compilation: ${if (result.compileResult.success) "✅ SUCCESS" else "❌ FAILED"}
        ${if (!result.compileResult.success) "Error: ${result.compileResult.output}" else ""}
        
        🧪 Tests: ${if (result.testResult.success) "✅ PASSED" else "❌ FAILED"}
        ${if (!result.testResult.success) "Error: ${result.testResult.output}" else ""}
        
        📦 Package: ${if (result.packageResult.success) "✅ SUCCESS" else "❌ FAILED"}
        
        🚀 Deploy: ${if (result.deployResult.success) "✅ SUCCESS" else "❌ FAILED"}
        
        Next steps:
        ${if (result.allSuccessful) {
            "• Agent is ready to use\n• Run: cd ${result.projectDir} && mvn spring-boot:run"
        } else {
            "• Fix compilation/test errors\n• Check logs: ${result.projectDir}/target/surefire-reports"
        }}
        """.trimIndent()
    }
}
```

### Configuration Properties

```yaml
# application.yml
meta-agent:
  workspace:
    base-directory: ${user.home}/.meta-agent/workspace
    cleanup-after-days: 30
    max-projects: 100
  
  pipeline:
    auto-compile: true
    auto-test: true
    auto-package: false
    auto-deploy: false
    maven-timeout: 300s
    
  generated-project:
    group-id: com.generated.agent
    version: 1.0.0-SNAPSHOT
    java-version: 17
```

### Generated Project Template

```xml
<!-- Generated pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.generated.agent</groupId>
    <artifactId>restaurant-agent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <properties>
        <java.version>17</java.version>
        <kotlin.version>1.9.21</kotlin.version>
    </properties>
    
    <dependencies>
        <!-- Embabel Agent API -->
        <dependency>
            <groupId>com.embabel</groupId>
            <artifactId>embabel-agent-api</artifactId>
            <version>1.0.0</version>
        </dependency>
        
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        
        <!-- Auto-discovered tool dependencies -->
        <dependency>
            <groupId>com.opentable</groupId>
            <artifactId>opentable-api</artifactId>
            <version>2.1.0</version>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <sourceDirectory>src/main/kotlin</sourceDirectory>
        <testSourceDirectory>src/test/kotlin</testSourceDirectory>
        
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### Generated Test Template

```kotlin
// Generated RestaurantAgentTest.kt
@SpringBootTest
@ExtendWith(MockitoExtension::class)
class RestaurantAgentTest {
    
    @Autowired
    private lateinit var restaurantAgent: RestaurantAgent
    
    @Test
    fun `should extract dining preferences from user input`() {
        // Generated test based on agent capabilities
        val userInput = UserInput("I want vegan Italian food in Berlin")
        
        val preferences = restaurantAgent.extractPreferences(userInput)
        
        assertThat(preferences.cuisine).isEqualTo("Italian")
        assertThat(preferences.dietaryRestrictions).contains("vegan")
    }
    
    @Test
    fun `should find restaurants with discovered tools`() {
        // Generated test using auto-discovered tools
        val preferences = DiningPreferences(
            cuisine = "Italian",
            dietaryRestrictions = listOf("vegan")
        )
        
        val results = restaurantAgent.findVeganRestaurants(preferences, "Berlin")
        
        assertThat(results).isNotNull()
        assertThat(results.results).isNotEmpty()
    }
    
    @Test
    @Disabled("Integration test - requires API keys")
    fun `should make actual restaurant booking`() {
        // Generated integration test (disabled by default)
        // Users can enable when they have API keys configured
    }
}
```

### Usage Examples

#### **Option A: Local Development (.m2 Repository)**
```bash
# Generate and deploy directly to local Maven repository
meta-agent:> generate "restaurant booking agent" --deploy

🎯 Agent Generation Pipeline Report

📁 Project: ~/.meta-agent/workspace/generated-agents/restaurant-booking-agent-20241216-143022/

🔨 Compilation: ✅ SUCCESS
🧪 Tests: ✅ PASSED
📦 Package: ✅ SUCCESS
🚀 Deploy: ✅ SUCCESS

Next steps:
• Agent is ready to use
• Available in local Maven repository: ~/.m2/repository/com/generated/agent/restaurant-booking-agent/1.0.0-SNAPSHOT/

# Use in other projects
<dependency>
    <groupId>com.generated.agent</groupId>
    <artifactId>restaurant-booking-agent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### **Option B: Workspace Development**
```bash
# Generate for workspace development
meta-agent:> generate "restaurant booking agent" --test

🎯 Agent Generation Pipeline Report

📁 Project: ~/.meta-agent/workspace/generated-agents/restaurant-booking-agent-20241216-143022/

🔨 Compilation: ✅ SUCCESS
🧪 Tests: ✅ PASSED
📦 Package: ✅ SUCCESS
🚀 Deploy: ❌ SKIPPED

Next steps:
• Agent is ready to use
• Run: cd ~/.meta-agent/workspace/generated-agents/restaurant-booking-agent-20241216-143022/ && mvn spring-boot:run

# Continue development in workspace
cd ~/.meta-agent/workspace/generated-agents/restaurant-booking-agent-20241216-143022/

# Make changes to generated code
vim src/main/kotlin/com/generated/agent/RestaurantAgent.kt

# Test changes
mvn test

# Run the agent
mvn spring-boot:run
```

#### **Testing Existing Generated Agents**
```bash
# Test specific generated agent
meta-agent:> test-agent ~/.meta-agent/workspace/generated-agents/restaurant-booking-agent-20241216-143022/

🧪 Test Results:
✅ RestaurantAgentTest.extractPreferences: PASSED
✅ RestaurantAgentTest.findVeganRestaurants: PASSED
⚠️  RestaurantAgentTest.makeActualBooking: SKIPPED (Integration test)

Overall: ✅ PASSED (2/2 tests)
```

#### **Deploying Existing Generated Agents**
```bash
# Deploy specific generated agent to local repository
meta-agent:> deploy-agent ~/.meta-agent/workspace/generated-agents/restaurant-booking-agent-20241216-143022/

🚀 Deploy Results:
✅ Installed to: ~/.m2/repository/com/generated/agent/restaurant-booking-agent/1.0.0-SNAPSHOT/
✅ Available for use in other Maven projects
```

### Development Pipeline Benefits

1. **🔄 Iterative Development**: Generate → Test → Refine → Test again
2. **📁 Organized Workspace**: Timestamped projects prevent conflicts
3. **🧪 Automated Testing**: Generated tests validate agent functionality
4. **🚀 Easy Deployment**: One command to deploy to local repository
5. **🔧 Standard Tools**: Uses familiar Maven commands and structure
6. **📦 Production Ready**: Generated projects are complete Spring Boot applications

This pipeline ensures generated agents are **production-ready** with proper testing, packaging, and deployment capabilities!

---

## 🎯 Enhanced Design: Annotation Knowledge, Generic Context & Recovery

### **Issue 1: LLM Annotation Knowledge**

**Challenge**: How does the LLM know about Embabel's `@Agent`, `@Action`, `@AchievableGoal` annotations?

**Solution**: Template-based generation with annotation examples embedded in prompts

```kotlin
// Enhanced prompt building with annotation examples
private fun buildEnhancedPrompt(context: MetaAgentContext, tools: List<ToolRecommendation>): String {
    val annotationExamples = buildAnnotationExamples(context.language)
    
    return """
    Generate a ${context.language} agent that ${context.task}.
    
    IMPORTANT: Use these exact Embabel Agent API annotations:
    
    ${annotationExamples}
    
    Available tools (auto-discovered):
    ${tools.joinToString("\n") { "- ${it.tool.name}: ${it.reasoning}" }}
    
    Generate complete agent following this EXACT structure:
    
    ```kotlin
    @Agent(
        description = "Description of what this agent does",
        scan = true
    )
    class GeneratedAgentModel(
        private val service: SomeService,
        @Value("\${agent.model:gpt-4.1-mini}") private val model: String,
    ) {
        
        @Action
        fun firstAction(input: InputType): OutputType = 
            usingModel(model).createObject("prompt for LLM")
        
        @Action(toolGroups = [${tools.joinToString { "\"${it.tool.toolGroup}\"" }}])
        fun secondAction(input: InputType): OutputType = 
            usingModel(model).createObject("prompt with tools")
        
        @AchievesGoal(description = "Final goal achievement")
        @Action
        fun finalAction(input: InputType): FinalOutputType = 
            // Implementation
    }
    ```
    
    Use these exact imports:
    import com.embabel.agent.api.annotation.*
    import com.embabel.agent.api.common.*
    import com.embabel.agent.core.CoreToolGroups
    import com.embabel.common.ai.model.LlmOptions
    """.trimIndent()
}

private fun buildAnnotationExamples(language: String): String {
    return when (language) {
        "kotlin" -> """
        @Agent(description = "Agent description", scan = true)
        @Action - marks action methods
        @Action(toolGroups = [CoreToolGroups.WEB, CoreToolGroups.MAPS])
        @AchievesGoal(description = "Goal description") - marks final action
        @Value("\${property.name:default}") - Spring property injection
        """
        "java" -> """
        @Agent(description = "Agent description", scan = true)
        @Action - marks action methods
        @Action(toolGroups = {CoreToolGroups.WEB, CoreToolGroups.MAPS})
        @AchievesGoal(description = "Goal description") - marks final action
        @Value("${property.name:default}") - Spring property injection
        """
        else -> ""
    }
}
```

### **Issue 2: Generic and Extensible MetaAgentContext**

**Challenge**: Make MetaAgentContext generic enough for all domains, not just restaurant/location-specific

**Solution**: Flexible properties with domain-specific builders


### **Generic Context Usage Examples**

```kotlin
// Generic usage
val context = MetaAgentContext(
    task = "weather forecasting agent",
    properties = mapOf(
        "location" to "London",
        "forecastDays" to 7,
        "units" to "metric"
    ),
    constraints = listOf("real-time updates", "accuracy > 85%"),
    securityLevel = SecurityLevel.BASIC
)

// Domain-specific builders
val restaurantContext = MetaAgentContext.restaurant(
    task = "fine dining reservation agent",
    location = "Paris",
    cuisine = "French",
    priceRange = "high-end"
)

val travelContext = MetaAgentContext.travel(
    task = "business travel booking agent",
    origin = "New York",
    destination = "London",
    dates = "2024-01-15 to 2024-01-20",
    travelers = 2
)

val paymentContext = MetaAgentContext.payment(
    task = "secure payment processing agent",
    currency = "USD",
    maxAmount = "10000",
    paymentMethods = listOf("credit_card", "paypal", "bank_transfer")
)
```

### **Issue 3: Build Recovery - Automated vs Human-in-the-Loop**

**Challenge**: Handle compilation failures with robust recovery mechanisms

**Solution**: Multi-strategy recovery system with automated and interactive modes

```kotlin
// Enhanced pipeline with failure handling and recovery
@Service
class GeneratedAgentPipeline(
    private val properties: MetaAgentProperties,
    private val mavenRunner: MavenRunner,
    private val metaAgent: MetaAgent,
    private val terminalServices: TerminalServices,
) {
    
    fun processGeneratedAgent(
        generatedAgent: GeneratedAgentModel, 
        context: MetaAgentContext
    ): PipelineResult {
        val workspaceDir = createWorkspaceDirectory(context)
        
        return try {
            // 1. Write generated code to workspace
            val projectDir = writeToWorkspace(generatedAgent, workspaceDir)
            
            // 2. Compile generated code (CRITICAL GATE)
            val compileResult = compileAgent(projectDir)
            if (!compileResult.success) {
                return PipelineResult.compilationFailure(compileResult, projectDir)
            }
            
            // 3. Run generated tests (CRITICAL GATE)
            val testResult = runTests(projectDir)
            if (!testResult.success && properties.pipeline.failOnTestFailure) {
                return PipelineResult.testFailure(testResult, projectDir)
            }
            
            // 4. Package and deploy (conditional)
            val packageResult = conditionalPackage(projectDir)
            val deployResult = conditionalDeploy(projectDir)
            
            PipelineResult.success(
                projectDir = projectDir,
                compileResult = compileResult,
                testResult = testResult,
                packageResult = packageResult,
                deployResult = deployResult
            )
        } catch (e: Exception) {
            PipelineResult.failure(e)
        }
    }
    
    // Auto-recovery with compilation feedback
    fun regenerateWithFeedback(
        originalContext: MetaAgentContext,
        compilationError: CompileResult,
        maxRetries: Int = 3
    ): PipelineResult {
        var attempt = 1
        var lastError = compilationError
        
        while (attempt <= maxRetries) {
            logger.info("🔄 Regeneration attempt $attempt/$maxRetries")
            
            // Build enhanced context with compilation feedback
            val enhancedContext = originalContext.copy(
                properties = originalContext.properties + mapOf(
                    "compilationError" to lastError.output,
                    "previousAttempt" to attempt - 1,
                    "fixRequired" to extractFixHints(lastError.output)
                )
            )
            
            // Regenerate agent with error context
            val regeneratedAgent = metaAgent.generateAgentWithErrorFeedback(
                enhancedContext, 
                lastError
            )
            
            // Try pipeline again
            val result = processGeneratedAgent(regeneratedAgent, enhancedContext)
            
            if (result.success) {
                return result.copy(
                    regenerationAttempts = attempt,
                    originalError = compilationError.output
                )
            }
            
            lastError = result.compileResult
            attempt++
        }
        
        return PipelineResult.regenerationFailure(lastError, attempt - 1)
    }
    
    // Human-in-the-loop recovery
    fun handleInteractiveRecovery(
        context: MetaAgentContext, 
        failedResult: PipelineResult
    ): PipelineResult {
        println("❌ Agent generation failed:")
        println(failedResult.compileResult.output)
        println()
        
        val choice = terminalServices.prompt(
            """
            Choose recovery option:
            1. Auto-retry with error feedback
            2. Show generated code for manual review
            3. Modify task description and regenerate
            4. Abort generation
            
            Your choice (1-4): 
            """.trimIndent()
        )
        
        return when (choice) {
            "1" -> {
                println("🔄 Attempting auto-recovery...")
                regenerateWithFeedback(context, failedResult.compileResult, 3)
            }
            
            "2" -> {
                showGeneratedCodeForReview(failedResult.projectDir)
                askForManualFixes(context, failedResult)
            }
            
            "3" -> {
                val newTask = terminalServices.prompt("Enter modified task description: ")
                val newContext = context.copy(task = newTask)
                val newAgent = metaAgent.generateAgent(newContext)
                processGeneratedAgent(newAgent, newContext)
            }
            
            "4" -> PipelineResult.userAborted()
            
            else -> PipelineResult.invalidChoice()
        }
    }
    
    // Smart recovery strategy
    fun handleSmartRecovery(
        context: MetaAgentContext, 
        result: PipelineResult
    ): PipelineResult {
        val errorType = classifyError(result.compileResult.output)
        
        return when (errorType) {
            ErrorType.SIMPLE_SYNTAX -> {
                println("🔄 Simple syntax error detected, attempting auto-recovery...")
                regenerateWithFeedback(context, result.compileResult, 2)
            }
            
            ErrorType.COMPLEX_LOGIC -> {
                println("🤔 Complex error detected, human input recommended...")
                handleInteractiveRecovery(context, result)
            }
            
            ErrorType.MISSING_DEPENDENCY -> {
                println("📦 Missing dependency detected, attempting auto-fix...")
                fixMissingDependency(context, result)
            }
            
            ErrorType.ANNOTATION_ERROR -> {
                println("🏷️ Annotation error detected, attempting auto-recovery...")
                regenerateWithFeedback(context, result.compileResult, 3)
            }
            
            else -> {
                println("❓ Unknown error type, trying auto-recovery first...")
                val autoResult = regenerateWithFeedback(context, result.compileResult, 2)
                if (!autoResult.success) {
                    println("Auto-recovery failed, switching to interactive mode...")
                    handleInteractiveRecovery(context, autoResult)
                } else {
                    autoResult
                }
            }
        }
    }
    
    private fun extractFixHints(compilationOutput: String): List<String> {
        val hints = mutableListOf<String>()
        
        when {
            compilationOutput.contains("cannot find symbol") -> {
                hints.add("Add missing imports")
                hints.add("Check class/method names")
            }
            compilationOutput.contains("incompatible types") -> {
                hints.add("Fix type mismatches")
                hints.add("Check generic type parameters")
            }
            compilationOutput.contains("annotation") -> {
                hints.add("Check annotation syntax")
                hints.add("Verify annotation imports")
            }
            compilationOutput.contains("package") -> {
                hints.add("Fix package declarations")
                hints.add("Check directory structure")
            }
        }
        
        return hints
    }
    
    private fun classifyError(output: String): ErrorType {
        return when {
            output.contains("cannot find symbol") -> ErrorType.MISSING_DEPENDENCY
            output.contains("annotation") -> ErrorType.ANNOTATION_ERROR
            output.contains("syntax error") -> ErrorType.SIMPLE_SYNTAX
            output.contains("method does not override") -> ErrorType.SIMPLE_SYNTAX
            output.contains("incompatible types") -> ErrorType.COMPLEX_LOGIC
            else -> ErrorType.UNKNOWN
        }
    }
}

enum class RecoveryMode {
    AUTO,           // Fully automated recovery
    INTERACTIVE,    // Human-in-the-loop
    SMART,          // Auto for simple errors, HIL for complex
    NONE            // No recovery, just report failure
}

enum class ErrorType {
    SIMPLE_SYNTAX,      // Missing semicolons, typos
    COMPLEX_LOGIC,      // Logic errors, algorithm issues
    MISSING_DEPENDENCY, // Missing imports, dependencies
    ANNOTATION_ERROR,   // Wrong annotations, syntax
    UNKNOWN
}
```

### **Enhanced Shell Commands with Recovery Modes**

```kotlin
@ShellMethod("Generate agent with smart recovery")
fun generateSmart(
    @ShellOption(help = "Agent description") task: String,
    @ShellOption(value = ["-t", "--tools"]) tools: List<String> = emptyList(),
    @ShellOption(value = ["-l", "--lang"]) language: String = "kotlin",
    @ShellOption(value = ["--recovery"]) recovery: RecoveryMode = RecoveryMode.SMART,
    @ShellOption(value = ["--max-retries"]) maxRetries: Int = 3,
): String {
    
    val context = MetaAgentContext(task, language, tools)
    val generatedAgent = metaAgent.generateAgent(context)
    
    var result = pipeline.processGeneratedAgent(generatedAgent, context)
    
    if (!result.success) {
        result = when (recovery) {
            RecoveryMode.AUTO -> {
                println("🔄 Auto-recovery mode enabled")
                pipeline.regenerateWithFeedback(context, result.compileResult, maxRetries)
            }
            
            RecoveryMode.INTERACTIVE -> {
                println("🤝 Interactive recovery mode enabled")
                pipeline.handleInteractiveRecovery(context, result)
            }
            
            RecoveryMode.SMART -> {
                println("🧠 Smart recovery mode enabled")
                pipeline.handleSmartRecovery(context, result)
            }
            
            RecoveryMode.NONE -> {
                println("⚠️ No recovery mode - reporting failure")
                result
            }
        }
    }
    
    return buildEnhancedPipelineReport(result)
}

@ShellMethod("Regenerate failed agent with specific recovery mode")
fun regenerate(
    @ShellOption(help = "Failed project directory") projectDir: String,
    @ShellOption(value = ["--recovery"]) recovery: RecoveryMode = RecoveryMode.SMART,
    @ShellOption(value = ["--max-retries"]) maxRetries: Int = 3,
): String {
    val path = Paths.get(projectDir)
    val context = extractContextFromProject(path)
    val lastError = getLastCompilationError(path)
    
    val result = when (recovery) {
        RecoveryMode.AUTO -> pipeline.regenerateWithFeedback(context, lastError, maxRetries)
        RecoveryMode.INTERACTIVE -> pipeline.handleInteractiveRecovery(context, PipelineResult.compilationFailure(lastError, path))
        RecoveryMode.SMART -> pipeline.handleSmartRecovery(context, PipelineResult.compilationFailure(lastError, path))
        RecoveryMode.NONE -> PipelineResult.compilationFailure(lastError, path)
    }
    
    return buildEnhancedPipelineReport(result)
}
```

### **Configuration for Recovery Modes**

```yaml
# application.yml
meta-agent:
  recovery:
    default-mode: SMART              # AUTO, INTERACTIVE, SMART, NONE
    max-auto-attempts: 3
    interactive-timeout: 300s        # How long to wait for user input
    
    # Auto-recovery settings
    auto-recovery:
      enabled: true
      simple-errors: true            # Auto-fix syntax errors
      annotation-errors: true        # Auto-fix annotation issues
      dependency-errors: true        # Auto-fix missing dependencies
      
    # Interactive settings
    interactive:
      show-code-on-failure: true
      offer-manual-edit: true
      suggest-fixes: true
      
    # Smart recovery settings
    smart:
      auto-threshold: 0.8           # Confidence threshold for auto-recovery
      complex-error-threshold: 0.3  # Below this, use interactive mode
```

### **Usage Examples with Recovery**

```bash
# Fully automated recovery (CI/CD friendly)
meta-agent:> generate "restaurant agent" --recovery AUTO
🔄 Auto-recovery mode enabled
🔄 Regeneration attempt 1/3
✅ Auto-recovery successful after 2 attempts

# Interactive recovery (development friendly)
meta-agent:> generate "complex trading agent" --recovery INTERACTIVE
🤝 Interactive recovery mode enabled
❌ Agent generation failed:
[compilation errors]

Choose recovery option:
1. Auto-retry with error feedback
2. Show generated code for manual review
3. Modify task description and regenerate
4. Abort generation

Your choice (1-4): 2

# Smart recovery (recommended default)
meta-agent:> generate "payment processor" --recovery SMART
🧠 Smart recovery mode enabled
🔄 Simple syntax error detected, attempting auto-recovery...
✅ Auto-recovery successful after 1 attempt

# Complex error with smart recovery
meta-agent:> generate "machine learning model trainer" --recovery SMART
🧠 Smart recovery mode enabled
🤔 Complex error detected, human input recommended...
[switches to interactive mode]
```

### **Benefits of Enhanced Design**

1. **🎯 Precise Annotation Generation**: LLM knows exactly which annotations to use
2. **🔧 Generic Context**: Supports any domain without hardcoded assumptions
3. **🔄 Intelligent Recovery**: Adapts recovery strategy to error complexity
4. **🤝 Human Choice**: Users control automation vs. manual intervention
5. **🚀 Production Ready**: Automated modes work in CI/CD environments
6. **📚 Learning System**: Improves over time by learning from common errors

This design provides **maximum flexibility** while maintaining **robust error handling** and **intelligent automation**!

---

## 🎯 Concrete Agent Generation with Full Type Safety

### **Production-Ready Code Generation**

Meta-agent generates **concrete, domain-specific agents** with full compile-time and runtime type safety - not generic `Map<String, Any>` but typed domain entities.

### **Example: Restaurant Agent with Concrete Types**

**Generated Domain Entities:**
```kotlin
// Generated domain-specific data classes
data class DiningPreferences(
    val cuisine: String,
    val priceRange: PriceRange,
    val dietaryRestrictions: List<String> = emptyList(),
    val partySize: Int,
    val preferredTime: LocalTime?
)

data class RestaurantSearchCriteria(
    val location: String,
    val preferences: DiningPreferences,
    val radius: Double = 5.0,
    val availabilityDate: LocalDate
)

data class RestaurantOption(
    val id: String,
    val name: String,
    val cuisine: String,
    val priceRange: PriceRange,
    val rating: Double,
    val availableTimes: List<LocalTime>,
    val location: Address
)

data class ReservationRequest(
    val restaurant: RestaurantOption,
    val dateTime: LocalDateTime,
    val partySize: Int,
    val specialRequests: String?
)

data class ReservationConfirmation(
    val confirmationNumber: String,
    val restaurant: RestaurantOption,
    val dateTime: LocalDateTime,
    val partySize: Int,
    val status: ReservationStatus
)

enum class PriceRange { BUDGET, MODERATE, UPSCALE, FINE_DINING }
enum class ReservationStatus { CONFIRMED, PENDING, CANCELLED }
```

**Generated Agent with Full Type Safety:**
```kotlin
@Agent(
    description = "Restaurant reservation agent with full type safety",
    scan = true
)
class RestaurantReservationAgent(
    private val openTableService: OpenTableService,
    private val yelpService: YelpService,
    @Value("\${restaurant.model:gpt-4.1-mini}") private val model: String,
) {
    
    @Action
    fun extractDiningPreferences(userInput: UserInput): DiningPreferences =
        usingModel(model).createObject(
            """
            Extract dining preferences from user input: "${userInput.content}"
            
            Return a DiningPreferences object with:
            - cuisine: type of food (Italian, Chinese, etc.)
            - priceRange: BUDGET, MODERATE, UPSCALE, or FINE_DINING
            - dietaryRestrictions: list of restrictions (vegetarian, gluten-free, etc.)
            - partySize: number of people
            - preferredTime: preferred dining time if mentioned
            """.trimIndent()
        )
    
    @Action(toolGroups = [CoreToolGroups.WEB, CoreToolGroups.MAPS])
    fun searchRestaurants(
        criteria: RestaurantSearchCriteria
    ): List<RestaurantOption> =
        usingModel(model).createObject(
            """
            Search for restaurants matching these criteria:
            - Location: ${criteria.location}
            - Cuisine: ${criteria.preferences.cuisine}
            - Price range: ${criteria.preferences.priceRange}
            - Party size: ${criteria.preferences.partySize}
            - Date: ${criteria.availabilityDate}
            
            Use Yelp for restaurant data and OpenTable for availability.
            Return a list of RestaurantOption objects with complete details.
            """.trimIndent()
        )
    
    @Action(toolGroups = [CoreToolGroups.WEB])
    fun checkAvailability(
        restaurant: RestaurantOption,
        date: LocalDate,
        partySize: Int
    ): List<LocalTime> =
        usingModel(model).createObject(
            """
            Check availability for ${restaurant.name} on ${date} for ${partySize} people.
            Return list of available time slots as LocalTime objects.
            """.trimIndent()
        )
    
    @AchievesGoal(description = "Complete restaurant reservation with confirmation")
    @Action(toolGroups = [CoreToolGroups.WEB])
    fun makeReservation(
        request: ReservationRequest
    ): ReservationConfirmation =
        usingModel(model).createObject(
            """
            Make a reservation at ${request.restaurant.name} for:
            - Date/Time: ${request.dateTime}
            - Party size: ${request.partySize}
            - Special requests: ${request.specialRequests ?: "None"}
            
            Return ReservationConfirmation with confirmation number and status.
            """.trimIndent()
        )
}
```

**Generated Test with Type Safety:**
```kotlin
@SpringBootTest
class RestaurantReservationAgentTest {
    
    @Autowired
    private lateinit var agent: RestaurantReservationAgent
    
    @Test
    fun `should extract dining preferences with correct types`() {
        val userInput = UserInput("I want Italian food for 4 people, vegetarian options, upscale dining")
        
        val preferences = agent.extractDiningPreferences(userInput)
        
        assertThat(preferences.cuisine).isEqualTo("Italian")
        assertThat(preferences.partySize).isEqualTo(4)
        assertThat(preferences.priceRange).isEqualTo(PriceRange.UPSCALE)
        assertThat(preferences.dietaryRestrictions).contains("vegetarian")
    }
    
    @Test
    fun `should search restaurants with type-safe criteria`() {
        val criteria = RestaurantSearchCriteria(
            location = "Berlin",
            preferences = DiningPreferences(
                cuisine = "Italian",
                priceRange = PriceRange.MODERATE,
                partySize = 2,
                preferredTime = LocalTime.of(19, 0)
            ),
            availabilityDate = LocalDate.now().plusDays(1)
        )
        
        val restaurants = agent.searchRestaurants(criteria)
        
        assertThat(restaurants).isNotEmpty()
        assertThat(restaurants.first()).isInstanceOf(RestaurantOption::class.java)
    }
    
    @Test
    fun `should complete full reservation flow with type safety`() {
        // Full end-to-end test with concrete types
        val userInput = UserInput("Book Italian restaurant for 2 people tomorrow at 7pm")
        val preferences = agent.extractDiningPreferences(userInput)
        val criteria = RestaurantSearchCriteria(
            location = "Berlin",
            preferences = preferences,
            availabilityDate = LocalDate.now().plusDays(1)
        )
        
        val restaurants = agent.searchRestaurants(criteria)
        val selectedRestaurant = restaurants.first()
        
        val availableTimes = agent.checkAvailability(
            selectedRestaurant,
            LocalDate.now().plusDays(1),
            preferences.partySize
        )
        
        val reservation = agent.makeReservation(
            ReservationRequest(
                restaurant = selectedRestaurant,
                dateTime = LocalDateTime.of(LocalDate.now().plusDays(1), availableTimes.first()),
                partySize = preferences.partySize,
                specialRequests = null
            )
        )
        
        assertThat(reservation.status).isEqualTo(ReservationStatus.CONFIRMED)
        assertThat(reservation.confirmationNumber).isNotBlank()
    }
}
```

## 🔍 Generated Agent Agentic Audit Framework *(Sprint 5 - Advanced Feature)*

### Audit Scope: GENERATED Agent Execution

The audit framework focuses specifically on tracking the **generated agent's runtime behavior**, not the meta-agent itself.

#### **Unified Audit Service**
```kotlin
// Generated agent includes: AuditService.kt
@Service
class AuditService {
    
    // Request Audit Events
    data class AgentRequestEvent(
        val timestamp: Instant,
        val agentId: String,
        val sessionId: String,
        val userRequest: String,                    // "fine french cuisine"
        val requestType: RequestType,
        val userId: String?,
        val contextData: Map<String, Any>
    )
    
    enum class RequestType {
        USER_REQUEST, SYSTEM_REQUEST, SCHEDULED_REQUEST, 
        CALLBACK_REQUEST, WEBHOOK_REQUEST
    }
    
    // Agentic Flow Events
    data class AgenticFlowEvent(
        val timestamp: Instant,
        val agentId: String,
        val sessionId: String,
        val userRequest: String,                    // Original request
        val goalState: com.embabel.agent.planning.GoalState,           // From embabel-agent-api
        val currentState: com.embabel.agent.planning.WorldState,       // From embabel-agent-api
        val executionPlan: List<com.embabel.agent.planning.Action>,    // From embabel-agent-api
        val planningTime: Duration,                // Time to generate plan
        val planComplexity: PlanComplexity
    )
    
    enum class PlanComplexity {
        SIMPLE, MODERATE, COMPLEX, VERY_COMPLEX
    }
    
    // Tool Usage Events
    data class ToolUsageEvent(
        val timestamp: Instant,
        val agentId: String,
        val sessionId: String,
        val actionName: String,                    // "searchRestaurants"
        val toolName: String,                      // "opentable_api"
        val toolEndpoint: String,                  // "GET /restaurants/search"
        val inputParameters: Map<String, Any>,     // search parameters (sanitized)
        val executionTime: Duration,               // how long the call took
        val success: Boolean,                      // did it succeed
        val responseCode: Int?,                    // HTTP response code
        val errorMessage: String?,                 // if failed
        val resultSummary: String?                 // high-level result summary
    )
    
    // Audit Methods
    fun auditRequest(event: AgentRequestEvent) {
        auditLogger.info("Agent request received", event)
        auditRepository.save(event)
        requestAnalyzer.analyzePattern(event)
    }
    
    fun auditAgenticFlow(event: AgenticFlowEvent) {
        auditLogger.info("Agentic flow execution", mapOf(
            "goalState" to event.goalState,
            "planSteps" to event.executionPlan.size,
            "planningTime" to event.planningTime,
            "complexity" to event.planComplexity
        ))
        auditRepository.save(event)
        planAnalyzer.analyzePlanEffectiveness(event)
    }
    
    fun auditToolUsage(event: ToolUsageEvent) {
        auditLogger.info("Tool usage", mapOf(
            "tool" to event.toolName,
            "endpoint" to event.toolEndpoint,
            "success" to event.success,
            "executionTime" to event.executionTime,
            "responseCode" to event.responseCode
        ))
        auditRepository.save(event)
        toolPerformanceTracker.recordUsage(event)
        
        // Detect anomalies
        if (event.executionTime > Duration.ofSeconds(30)) {
            anomalyDetector.reportSlowTool(event)
        }
    }
}
```

### **Generated Agent Audit Integration**

#### **Audit Configuration in Generated Agent**
```kotlin
// Generated in each agent: AuditConfiguration.kt
@Configuration
@EnableAudit
class AuditConfiguration {
    
    @Bean
    fun auditProperties(): AuditProperties {
        return AuditProperties(
            enabled = true,
            agentId = "restaurant-finder-agent-v1",
            auditLevel = AuditLevel.DETAILED,
            retentionDays = 90,
            encryptionEnabled = true
        )
    }
}
```

#### **AOP-Based Audit Implementation**
```kotlin
// Generated agent actions use clean annotations
@Agent(description = "Restaurant finder agent")
class RestaurantFinderAgent {
    
    @AuditRequest
    @AuditAgenticFlow
    @Action
    fun findRestaurants(userInput: UserInput): List<Restaurant> {
        // Pure business logic - audit happens transparently via AOP
        return planExecutor.execute(
            goal = GoalState(
                goalType = "FIND_RESTAURANT",
                parameters = mapOf(
                    "cuisine" to userInput.cuisine,
                    "location" to userInput.location
                )
            )
        )
    }
    
    @AuditToolUsage
    @Action(toolGroups = [ToolGroups.RESTAURANT_SEARCH])
    fun searchOpenTable(criteria: SearchCriteria): List<Restaurant> {
        // Pure business logic - tool usage audited via AOP
        return openTableClient.searchRestaurants(criteria)
    }
}

// Generated audit aspects for transparent auditing
@Aspect
@Component
class AuditAspect(
    private val auditService: AuditService
) {
    
    @Around("@annotation(AuditRequest)")
    fun auditRequest(joinPoint: ProceedingJoinPoint): Any? {
        val startTime = System.currentTimeMillis()
        
        try {
            // Audit request before execution
            auditService.auditRequest(
                AgentRequestEvent(
                    timestamp = Instant.now(),
                    agentId = extractAgentId(joinPoint),
                    sessionId = extractSessionId(joinPoint),
                    userRequest = extractUserRequest(joinPoint),
                    requestType = RequestType.USER_REQUEST,
                    userId = extractUserId(joinPoint),
                    contextData = extractContextData(joinPoint)
                )
            )
            
            // Execute the actual method
            val result = joinPoint.proceed()
            
            // Audit successful completion
            auditService.auditRequestCompletion(
                requestId = extractRequestId(joinPoint),
                success = true,
                executionTime = Duration.ofMillis(System.currentTimeMillis() - startTime),
                result = result
            )
            
            return result
            
        } catch (e: Exception) {
            // Audit failure
            auditService.auditRequestCompletion(
                requestId = extractRequestId(joinPoint),
                success = false,
                executionTime = Duration.ofMillis(System.currentTimeMillis() - startTime),
                error = e.message
            )
            throw e
        }
    }
    
    @Around("@annotation(AuditAgenticFlow)")
    fun auditAgenticFlow(joinPoint: ProceedingJoinPoint): Any? {
        val startTime = System.currentTimeMillis()
        
        // Capture planning state before execution
        val goalState = extractGoalState(joinPoint)
        val worldState = extractWorldState(joinPoint)
        
        try {
            val result = joinPoint.proceed()
            
            // Audit the complete agentic flow
            auditService.auditAgenticFlow(
                AgenticFlowEvent(
                    timestamp = Instant.now(),
                    agentId = extractAgentId(joinPoint),
                    sessionId = extractSessionId(joinPoint),
                    userRequest = extractUserRequest(joinPoint),
                    goalState = goalState,
                    currentState = worldState,
                    executionPlan = extractExecutionPlan(joinPoint),
                    planningTime = Duration.ofMillis(System.currentTimeMillis() - startTime),
                    planComplexity = calculatePlanComplexity(result)
                )
            )
            
            return result
            
        } catch (e: Exception) {
            // Audit flow failure
            auditService.auditAgenticFlowFailure(
                agentId = extractAgentId(joinPoint),
                sessionId = extractSessionId(joinPoint),
                goalState = goalState,
                error = e.message
            )
            throw e
        }
    }
    
    @Around("@annotation(AuditToolUsage)")
    fun auditToolUsage(joinPoint: ProceedingJoinPoint): Any? {
        val startTime = System.currentTimeMillis()
        
        try {
            val result = joinPoint.proceed()
            
            // Audit successful tool usage
            auditService.auditToolUsage(
                ToolUsageEvent(
                    timestamp = Instant.now(),
                    agentId = extractAgentId(joinPoint),
                    sessionId = extractSessionId(joinPoint),
                    actionName = joinPoint.signature.name,
                    toolName = extractToolName(joinPoint),
                    toolEndpoint = extractToolEndpoint(joinPoint),
                    inputParameters = sanitizeParameters(joinPoint.args),
                    executionTime = Duration.ofMillis(System.currentTimeMillis() - startTime),
                    success = true,
                    responseCode = extractResponseCode(result),
                    resultSummary = summarizeResult(result)
                )
            )
            
            return result
            
        } catch (e: Exception) {
            // Audit tool usage failure
            auditService.auditToolUsage(
                ToolUsageEvent(
                    timestamp = Instant.now(),
                    agentId = extractAgentId(joinPoint),
                    sessionId = extractSessionId(joinPoint),
                    actionName = joinPoint.signature.name,
                    toolName = extractToolName(joinPoint),
                    toolEndpoint = extractToolEndpoint(joinPoint),
                    inputParameters = sanitizeParameters(joinPoint.args),
                    executionTime = Duration.ofMillis(System.currentTimeMillis() - startTime),
                    success = false,
                    errorMessage = e.message
                )
            )
            throw e
        }
    }
}
```

### **Audit Reporting for Generated Agents**

#### **Generated Agent Audit Reports**
```kotlin
// Generated in each agent: AuditReportingService.kt
@Service
class GeneratedAgentAuditReportingService {
    
    fun generateUsageReport(timeRange: TimeRange): AgentUsageReport {
        return AgentUsageReport(
            totalRequests = auditRepository.countRequests(timeRange),
            successfulRequests = auditRepository.countSuccessfulRequests(timeRange),
            failedRequests = auditRepository.countFailedRequests(timeRange),
            averageResponseTime = auditRepository.calculateAverageResponseTime(timeRange),
            toolUsageBreakdown = auditRepository.getToolUsageBreakdown(timeRange),
            planningEfficiency = auditRepository.calculatePlanningEfficiency(timeRange)
        )
    }
    
    fun generatePerformanceReport(timeRange: TimeRange): AgentPerformanceReport {
        return AgentPerformanceReport(
            averagePlanningTime = auditRepository.calculateAveragePlanningTime(timeRange),
            toolPerformanceMetrics = auditRepository.getToolPerformanceMetrics(timeRange),
            errorAnalysis = auditRepository.analyzeErrors(timeRange),
            userSatisfactionMetrics = auditRepository.getUserSatisfactionMetrics(timeRange)
        )
    }
}
```

### **Generated Agent Configuration**

```properties
# Generated Agent Audit Configuration
embabel.agent.audit.enabled=true
embabel.agent.audit.agent-id=${AGENT_ID}
embabel.agent.audit.level=DETAILED
embabel.agent.audit.retention-days=90

# Request Audit
embabel.agent.audit.requests.enabled=true
embabel.agent.audit.requests.include-parameters=true
embabel.agent.audit.requests.sanitize-sensitive=true

# GOAP Execution Audit
embabel.agent.audit.goap.enabled=true
embabel.agent.audit.goap.include-plan-details=true
embabel.agent.audit.goap.track-performance=true

# Tool Usage Audit
embabel.agent.audit.tools.enabled=true
embabel.agent.audit.tools.include-parameters=true
embabel.agent.audit.tools.sanitize-credentials=true
embabel.agent.audit.tools.track-performance=true

# Audit Storage
embabel.agent.audit.database.url=${AUDIT_DATABASE_URL}
embabel.agent.audit.database.encryption-key=${AUDIT_ENCRYPTION_KEY}

# User-Defined Logging Theme for Generated Agent
embabel.agent.logging.theme=${AGENT_THEME:restaurant-agent}
embabel.agent.logging.palette.primary=${THEME_PRIMARY_COLOR:0x8b4513}    # Saddle brown
embabel.agent.logging.palette.secondary=${THEME_SECONDARY_COLOR:0xdaa520} # Goldenrod
embabel.agent.logging.palette.accent=${THEME_ACCENT_COLOR:0x228b22}       # Forest green
embabel.agent.logging.quotes.enabled=true
embabel.agent.logging.quotes.source=${THEME_QUOTES_FILE:restaurant-quotes.txt}
embabel.agent.logging.banner.enabled=true
embabel.agent.logging.banner.ascii-art=${THEME_ASCII_ART:restaurant-banner.txt}
```

#### **Generated Agent Logging Theme Implementation**

```kotlin
// Generated in each agent: LoggingConfiguration.kt
@Configuration
@Profile("${embabel.agent.logging.theme}")
class GeneratedAgentLoggingConfiguration(
    @Value("${embabel.agent.logging.palette.primary:0x8b4513}")
    private val primaryColor: String,
    
    @Value("${embabel.agent.logging.palette.secondary:0xdaa520}")
    private val secondaryColor: String,
    
    @Value("${embabel.agent.logging.palette.accent:0x228b22}")
    private val accentColor: String,
    
    @Value("${embabel.agent.logging.quotes.source:restaurant-quotes.txt}")
    private val quotesFile: String,
    
    @Value("${embabel.agent.logging.banner.ascii-art:restaurant-banner.txt}")
    private val bannerFile: String
) {
    
    @Bean
    fun userDefinedColorPalette(): ColorPalette {
        return object : ColorPalette {
            override val highlight: Int = primaryColor.removePrefix("0x").toInt(16)
            override val color2: Int = secondaryColor.removePrefix("0x").toInt(16)
            val accent: Int = accentColor.removePrefix("0x").toInt(16)
        }
    }
    
    @Bean
    fun userDefinedLoggingPersonality(): LoggingPersonality {
        return UserDefinedLoggingPersonality(
            colorPalette = userDefinedColorPalette(),
            quotesResource = quotesFile,
            bannerResource = bannerFile,
            agentName = "RestaurantFinderAgent"  // Generated based on agent type
        )
    }
}

// Generated logging personality for each agent
class UserDefinedLoggingPersonality(
    private val colorPalette: ColorPalette,
    private val quotesResource: String,
    private val bannerResource: String,
    private val agentName: String
) : LoggingPersonality {
    
    private val quotes: List<String> by lazy {
        loadQuotesFromResource(quotesResource)
    }
    
    private val banner: String by lazy {
        loadBannerFromResource(bannerResource)
    }
    
    override fun getWelcomeMessage(): String {
        return buildString {
            appendLine(colorize(banner, colorPalette.highlight))
            appendLine()
            appendLine(colorize("🤖 $agentName Ready", colorPalette.color2))
            appendLine(colorize("📝 ${getRandomQuote()}", colorPalette.accent))
            appendLine(colorize("=" * BANNER_WIDTH, colorPalette.highlight))
        }
    }
    
    override fun formatEventMessage(event: AgenticEvent): String {
        return when (event) {
            is AgentRequestEvent -> colorize("🎯 Request: ${event.userRequest}", colorPalette.highlight)
            is AgenticFlowEvent -> colorize("🔄 Planning: ${event.planComplexity}", colorPalette.color2)
            is ToolUsageEvent -> colorize("🔧 Tool: ${event.toolName}", colorPalette.accent)
            else -> event.toString()
        }
    }
    
    private fun getRandomQuote(): String {
        return quotes.randomOrNull() ?: "Ready to assist with your requests!"
    }
    
    private fun loadQuotesFromResource(resource: String): List<String> {
        return try {
            javaClass.classLoader.getResourceAsStream("logging/$resource")
                ?.bufferedReader()
                ?.readLines()
                ?.filter { it.isNotBlank() }
                ?: getDefaultQuotes()
        } catch (e: Exception) {
            getDefaultQuotes()
        }
    }
    
    private fun getDefaultQuotes(): List<String> {
        return listOf(
            "Generating solutions for your needs...",
            "AI-powered assistance at your service!",
            "Ready to tackle complex tasks efficiently!",
            "Bringing intelligence to automation!",
            "Your digital assistant is online!"
        )
    }
}
```

#### **User-Defined Resource Files**

**Example: restaurant-quotes.txt**
```
Bon appétit! Ready to find your perfect dining experience.
Great food brings people together - let's find yours!
From casual bites to fine dining, I've got you covered.
Every meal is an adventure waiting to happen.
Taste is subjective, but great service is universal.
Food is not just eating energy. It's an experience.
The best restaurants are where memories are made.
Hungry? Let's discover something delicious together!
```

**Example: restaurant-banner.txt**
```
    ╔══════════════════════════════════════════════════════════════════════════════════════════════╗
    ║                                  🍽️  RESTAURANT FINDER AGENT  🍽️                              ║
    ║                                     Your Culinary Companion                                  ║
    ╚══════════════════════════════════════════════════════════════════════════════════════════════╝
```

This focused audit framework ensures:
- **Complete traceability** of generated agent behavior
- **GOAP execution tracking** with planning details
- **Tool usage monitoring** with performance metrics
- **User request tracking** for behavior analysis
- **Generated agent-specific** audit capabilities

### **Meta-Agent Self-Evolution**

The generated agent audit data can be utilized for **meta-agent self-evolution**, enabling continuous improvement of the generation process:

```kotlin
// meta-agent-core/src/main/kotlin/com/embabel/metaagent/evolution/SelfEvolutionService.kt
@Service
class SelfEvolutionService {
    
    fun analyzeGeneratedAgentPerformance(): EvolutionInsights {
        // Analyze audit data from all generated agents
        val auditData = aggregateAuditData()
        
        return EvolutionInsights(
            commonFailurePatterns = identifyFailurePatterns(auditData),
            toolEfficiencyMetrics = analyzeToolEfficiency(auditData),
            planningOptimizations = identifyPlanningOptimizations(auditData),
            codeGenerationImprovements = suggestCodeImprovements(auditData)
        )
    }
    
    fun evolveGenerationTemplates(insights: EvolutionInsights) {
        // Update generation templates based on audit insights
        templateEvolutionService.updateTemplates(insights)
        
        // Improve tool discovery patterns
        toolDiscoveryEvolutionService.refineMethods(insights)
        
        // Enhance GOAP planning efficiency
        planningEvolutionService.optimizeStrategies(insights)
    }
}
```

This creates a **feedback loop** where:
- **Generated agents** provide audit data during execution
- **Meta-agent** analyzes this data to identify improvement opportunities
- **Generation process** evolves based on real-world agent performance
- **Future agents** benefit from continuous optimization

---

### **Benefits of Concrete Type-Safe Generation**

1. **🔒 Compile-time Safety**: No `ClassCastException` at runtime
2. **🎯 IDE Support**: Full autocomplete and refactoring capabilities
3. **📖 Self-documenting**: Clear contracts between actions
4. **🧪 Testable**: Easy to write unit tests with concrete types
5. **🔧 Maintainable**: Changes propagate through type system
6. **🚀 Production Ready**: Robust error handling and validation

---

## 🚀 Ready for Implementation

The meta-agent architecture is now **complete and production-ready**:

✅ **Multi-language JVM support** with proper annotation generation  
✅ **Generic MetaAgentContext** supporting any domain  
✅ **Intelligent tool autodiscovery** with smart defaults  
✅ **Robust build pipeline** with automated recovery  
✅ **Complete type safety** with concrete domain entities  
✅ **Interactive shell** integration with Embabel ecosystem  
✅ **Comprehensive testing** and deployment workflows  

**Next Step: Begin iterative implementation starting with Commit 1 - Project Structure Setup**
