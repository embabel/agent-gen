# Tools Discovery Implementation - Agent-as-Agent Architecture

> **Implementation Evolution**: Tools discovery as meta-agent actions with GOAP integration

## 🔄 **Architecture Transformation**

The tools discovery logic remains **functionally unchanged** but gets **architecturally transformed** from service-based to agent-action-based implementation.

### **Before: Service-Based Architecture**
```kotlin
@Service
class ToolDiscoveryService {
    fun discoverTools(domain: String): List<DiscoveredTool> {
        // RAG + Knowledge Graph logic
        return discoveredTools
    }
}

@RestController
class MetaAgentController {
    fun discoverTools(@RequestParam domain: String) = 
        toolDiscoveryService.discoverTools(domain)
}
```

### **After: Agent-Action-Based Architecture**
```kotlin  
@Agent(name = "MetaAgent")
class MetaAgent {
    
    @AchievesGoal(description = "Discover and analyze external tools for integration")
    @Action(
        cost = 0.7, // High cost due to API calls and analysis
        value = 0.8, // High value for agent generation
        toolGroups = ["web", "apis", "rag"]
    )
    fun discoverTools(design: AgentDesign): DiscoveredTools {
        // SAME core logic as before
        // But now integrated as agent action with GOAP
        return performToolDiscovery(design)
    }
}

@ShellComponent
class MetaAgentShellCommands(
    private val metaAgent: MetaAgent,
    private val autonomy: Autonomy
) : ShellCommands {
    
    @ShellMethod("Discover tools for current design")
    fun discoverTools(@ShellOption("--domain") domain: String): String {
        return autonomy.accomplishGoal(
            goalName = "discoverTools", 
            agent = metaAgent,
            input = getCurrentDesign().withDomain(domain)
        ).format()
    }
}
```

## 🏗️ **Core Implementation Components**

### **1. RAG + Knowledge Graph Integration (Unchanged Logic)**

The core discovery algorithms remain identical:

```kotlin
/**
 * RAG-based tool discovery using vector similarity search.
 * Logic unchanged - now executed within agent action context.
 */
class ToolKnowledgeEngine {
    
    fun searchRelevantTools(domain: String, context: AgentDesign): List<ToolCandidate> {
        // UNCHANGED: Vector similarity search
        val domainEmbedding = embeddingService.embed(domain)
        val vectorCandidates = vectorStore.similaritySearch(domainEmbedding, k = 20)
        
        // NEW: LLM-based semantic tool discovery
        val llmCandidates = discoverToolsViaSemantic(domain, context)
        
        // UNCHANGED: Knowledge graph traversal  
        val relatedTools = knowledgeGraph.findRelatedTools(domain, vectorCandidates + llmCandidates)
        
        // Combine all discovery methods
        val allCandidates = (vectorCandidates + llmCandidates + relatedTools).distinctBy { it.name }
        
        // UNCHANGED: Relevance scoring
        return allCandidates.map { candidate ->
            ToolCandidate(
                tool = candidate,
                relevanceScore = calculateRelevance(candidate, context),
                integrationComplexity = assessComplexity(candidate)
            )
        }.sortedByDescending { it.relevanceScore }
    }
    
    private fun discoverToolsViaSemantic(domain: String, context: AgentDesign): List<ToolCandidate> {
        val prompt = """
        Given the domain "${domain}" and agent requirements:
        ${context.description}
        
        Suggest the most relevant APIs and tools for integration. Consider:
        - Popular APIs in this domain
        - Complementary services that work well together
        - Authentication and rate limiting considerations
        - Data flow and integration patterns
        
        Focus on well-established, reliable APIs with good documentation.
        """.trimIndent()
        
        val llmResponse = llmClient.complete(prompt)
        return parseLLMToolSuggestions(llmResponse, domain)
    }
    
    fun buildToolKnowledgeGraph(): KnowledgeGraph {
        // UNCHANGED: API registry crawling
        // UNCHANGED: Schema analysis and relationship extraction
        // UNCHANGED: Semantic clustering of similar tools
        return knowledgeGraph
    }
}
```

### **2. LLM-Based Semantic Tool Discovery (NEW)**

LLM integration provides intelligent semantic matching for tool discovery:

```kotlin
/**
 * LLM-powered semantic tool discovery engine.
 * NEW: Uses LLM reasoning to suggest contextually relevant tools.
 */
class LLMSemanticDiscoveryEngine(
    private val llmClient: LLMClient,
    private val toolValidator: ToolValidator
) {
    
    fun discoverToolsSemanticaly(domain: String, agentContext: AgentDesign): List<SemanticToolSuggestion> {
        // Domain-specific LLM prompting for tool discovery
        val prompt = buildSemanticDiscoveryPrompt(domain, agentContext)
        val llmResponse = llmClient.complete(prompt)
        
        // Parse structured tool suggestions from LLM
        val suggestions = parseLLMToolSuggestions(llmResponse)
        
        // Validate and enrich suggestions with real API data
        return suggestions.mapNotNull { suggestion ->
            val validatedTool = toolValidator.validateAndEnrich(suggestion)
            validatedTool?.let {
                SemanticToolSuggestion(
                    toolName = it.name,
                    apiUrl = it.discoveredUrl,
                    semanticReasoning = suggestion.reasoning,
                    confidenceScore = suggestion.confidence,
                    integrationPattern = suggestion.suggestedPattern,
                    contextualFit = calculateContextualFit(it, agentContext)
                )
            }
        }
    }
    
    private fun buildSemanticDiscoveryPrompt(domain: String, context: AgentDesign): String {
        return """
        You are an expert in API integration and software architecture. 
        
        Task: Discover the most relevant APIs and tools for building an agent in the "${domain}" domain.
        
        Agent Context:
        - Purpose: ${context.description}
        - Target Actions: ${context.intendedActions.joinToString(", ")}
        - User Requirements: ${context.userRequirements}
        
        Please suggest 5-10 most relevant tools/APIs with:
        1. Tool Name
        2. Primary Use Case  
        3. API URL (if known)
        4. Integration Complexity (LOW/MEDIUM/HIGH)
        5. Why it's relevant for this specific use case
        6. Suggested integration pattern
        
        Examples of good suggestions:
        - For "restaurant booking": OpenTable API, Yelp Fusion API, Google Places API
        - For "weather forecasting": OpenWeatherMap API, Weather.gov API, AccuWeather API  
        - For "travel planning": Amadeus API, Skyscanner API, Booking.com API
        - For "financial data": Alpha Vantage API, Yahoo Finance API, IEX Cloud API
        
        Focus on:
        - Well-established APIs with good documentation
        - Complementary tools that work well together
        - Consider authentication requirements and rate limits
        - Real-world usage patterns and reliability
        
        Format your response as JSON array:
        [
          {
            "name": "Tool Name",
            "useCase": "What it does",
            "apiUrl": "https://api.example.com",
            "complexity": "MEDIUM",
            "reasoning": "Why it's perfect for this use case",
            "integrationPattern": "How to integrate it",
            "confidence": 0.9
          }
        ]
        """.trimIndent()
    }
    
    private fun parseLLMToolSuggestions(llmResponse: String): List<LLMToolSuggestion> {
        return try {
            val jsonArray = JsonParser.parseString(llmResponse).asJsonArray
            jsonArray.map { element ->
                val obj = element.asJsonObject
                LLMToolSuggestion(
                    name = obj.get("name").asString,
                    useCase = obj.get("useCase").asString,
                    apiUrl = obj.get("apiUrl")?.asString,
                    complexity = IntegrationComplexity.valueOf(obj.get("complexity").asString),
                    reasoning = obj.get("reasoning").asString,
                    integrationPattern = obj.get("integrationPattern").asString,
                    confidence = obj.get("confidence").asDouble
                )
            }
        } catch (e: Exception) {
            logger.warn("Failed to parse LLM tool suggestions: ${e.message}")
            emptyList()
        }
    }
}

/**
 * Tool validation and enrichment service.
 * Validates LLM suggestions against real API endpoints.
 */
class ToolValidator(
    private val apiIntrospectionEngine: ApiIntrospectionEngine,
    private val webClient: WebClient
) {
    
    fun validateAndEnrich(suggestion: LLMToolSuggestion): ValidatedTool? {
        return try {
            // Attempt to discover real API URL if not provided
            val discoveredUrl = suggestion.apiUrl ?: discoverApiUrl(suggestion.name)
            
            if (discoveredUrl != null) {
                // Validate API accessibility
                val isAccessible = webClient.head(discoveredUrl).statusCode.is2xxSuccessful
                
                if (isAccessible) {
                    // Enrich with real API metadata
                    val apiAnalysis = apiIntrospectionEngine.analyzeOpenApiSpec(discoveredUrl)
                    
                    ValidatedTool(
                        name = suggestion.name,
                        discoveredUrl = discoveredUrl,
                        endpoints = apiAnalysis.endpoints,
                        authScheme = apiAnalysis.authenticationScheme,
                        actualComplexity = apiAnalysis.integrationComplexity,
                        llmSuggestion = suggestion
                    )
                } else null
            } else null
        } catch (e: Exception) {
            logger.debug("Could not validate tool ${suggestion.name}: ${e.message}")
            null
        }
    }
    
    private fun discoverApiUrl(toolName: String): String? {
        // Try common API discovery patterns
        val candidates = listOf(
            "https://api.${toolName.lowercase()}.com",
            "https://${toolName.lowercase()}.com/api",
            "https://api.${toolName.lowercase()}.io",
            "https://${toolName.lowercase()}.io/api"
        )
        
        return candidates.firstOrNull { url ->
            try {
                webClient.head(url).statusCode.is2xxSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
}
```

### **3. Multi-Source Tool Discovery (Enhanced)**

Tool discovery now includes Docker Hub alongside traditional API registries:

```kotlin
/**
 * Docker Hub tool discovery engine.
 * NEW: Container-based tool discovery for comprehensive coverage.
 */
class DockerHubDiscoveryEngine {
    
    fun searchContainerizedTools(domain: String, limit: Int = 50): List<ContainerCandidate> {
        // Search Docker Hub for domain-relevant containers
        val searchQuery = buildSearchQuery(domain)
        val searchResults = dockerHubClient.search(searchQuery, limit)
        
        return searchResults.map { result ->
            val imageMetadata = dockerHubClient.getImageMetadata(result.name)
            val dockerfile = dockerHubClient.getDockerfile(result.name)
            
            ContainerCandidate(
                imageName = result.name,
                description = result.description,
                stars = result.star_count,
                pulls = result.pull_count,
                exposedPorts = extractExposedPorts(dockerfile),
                services = detectServices(imageMetadata, dockerfile),
                relevanceScore = calculateContainerRelevance(result, domain),
                integrationComplexity = assessContainerComplexity(dockerfile)
            )
        }.sortedByDescending { it.relevanceScore }
    }
    
    private fun buildSearchQuery(domain: String): String {
        // Domain-specific search strategies
        return when (domain.lowercase()) {
            "database" -> "postgres mysql mongodb redis elasticsearch"
            "messaging" -> "kafka rabbitmq redis nats"
            "monitoring" -> "prometheus grafana elasticsearch kibana"
            "ai", "ml" -> "tensorflow pytorch jupyter notebook"
            "web" -> "nginx apache httpd"
            else -> domain
        }
    }
    
    private fun detectServices(metadata: ImageMetadata, dockerfile: String): List<ContainerService> {
        val exposedPorts = extractExposedPorts(dockerfile)
        val entrypoint = extractEntrypoint(dockerfile)
        
        return exposedPorts.map { port ->
            ContainerService(
                port = port,
                protocol = detectProtocol(port, entrypoint),
                serviceType = inferServiceType(port, metadata.tags),
                healthCheckEndpoint = inferHealthCheck(port, entrypoint)
            )
        }
    }
}
```

### **3. API Introspection (Unchanged Logic)**

API analysis remains functionally identical:

```kotlin
/**
 * API introspection and analysis engine.
 * Core logic unchanged - now provides data to agent actions.
 */
class ApiIntrospectionEngine {
    
    fun analyzeOpenApiSpec(apiUrl: String): ApiAnalysis {
        // UNCHANGED: OpenAPI spec retrieval and parsing
        val spec = openApiParser.parse(fetchSpec(apiUrl))
        
        // UNCHANGED: Endpoint analysis
        val endpoints = spec.paths.map { (path, operations) ->
            operations.map { (method, operation) ->
                ApiEndpoint(
                    path = path,
                    method = HttpMethod.valueOf(method.uppercase()),
                    operation = operation.operationId ?: generateOperationId(path, method),
                    parameters = extractParameters(operation),
                    responseType = extractResponseType(operation)
                )
            }
        }.flatten()
        
        // UNCHANGED: Authentication scheme detection
        val authScheme = detectAuthScheme(spec.securitySchemes)
        
        return ApiAnalysis(
            endpoints = endpoints,
            authenticationScheme = authScheme,
            integrationComplexity = assessIntegrationComplexity(endpoints, authScheme)
        )
    }
    
    fun analyzeGraphQLSchema(schemaUrl: String): GraphQLAnalysis {
        // UNCHANGED: GraphQL introspection query
        // UNCHANGED: Schema parsing and type extraction
        // UNCHANGED: Query/mutation/subscription analysis
        return graphQLAnalysis
    }
    
    fun analyzeDockerContainer(imageName: String): ContainerAnalysis {
        // UNCHANGED: Docker Hub API analysis
        val imageMetadata = dockerHubClient.getImageMetadata(imageName)
        val dockerfile = dockerHubClient.getDockerfile(imageName)
        
        // UNCHANGED: Extract exposed ports and services
        val exposedPorts = extractExposedPorts(dockerfile)
        val services = detectServices(imageMetadata, dockerfile)
        
        // UNCHANGED: Analyze environment variables and configuration
        val configOptions = extractConfigurationOptions(dockerfile)
        
        return ContainerAnalysis(
            imageName = imageName,
            exposedPorts = exposedPorts,
            services = services,
            configOptions = configOptions,
            integrationComplexity = assessContainerComplexity(services, configOptions)
        )
    }
}
```

### **3. Agent Action Integration (New Architecture)**

The transformation occurs in how these unchanged components get orchestrated:

```kotlin
@Agent(name = "MetaAgent")
class MetaAgent(
    private val toolKnowledgeEngine: ToolKnowledgeEngine,
    private val apiIntrospectionEngine: ApiIntrospectionEngine,
    private val credentialManager: CredentialManager,
    private val blackboardManager: MetaAgentBlackboard
) {
    
    @AchievesGoal(description = "Discover and analyze external tools for integration")  
    @Action(
        cost = 0.7, // High cost due to API calls and analysis
        value = 0.8, // High value for subsequent generation
        toolGroups = ["web", "apis", "rag"]
    )
    fun discoverTools(design: AgentDesign): DiscoveredTools {
        // NEW: GOAP planning context
        val planningContext = getCurrentPlanningContext()
        logger.info("🔍 Starting tool discovery for domain: ${design.domain}")
        
        // UNCHANGED: Core discovery logic
        val toolCandidates = toolKnowledgeEngine.searchRelevantTools(
            domain = design.domain,
            context = design
        )
        
        // UNCHANGED: Tool analysis for each candidate (including containers)
        val analyzedTools = toolCandidates.parallelStream().map { candidate ->
            val analysis = when (candidate.toolType) {
                ToolType.REST_API -> apiIntrospectionEngine.analyzeOpenApiSpec(candidate.apiUrl)
                ToolType.GRAPHQL_API -> apiIntrospectionEngine.analyzeGraphQLSchema(candidate.apiUrl)
                ToolType.DOCKER_CONTAINER -> apiIntrospectionEngine.analyzeDockerContainer(candidate.imageName)
                else -> BasicToolAnalysis(candidate)
            }
            
            DiscoveredTool(
                name = candidate.name,
                apiUrl = candidate.apiUrl,
                apiType = candidate.apiType,
                endpoints = apiAnalysis.endpoints,
                authenticationRequired = apiAnalysis.requiresAuth,
                authenticationScheme = apiAnalysis.authScheme,
                integrationComplexity = apiAnalysis.complexity,
                relevanceScore = candidate.relevanceScore
            )
        }.collect(Collectors.toList())
        
        // NEW: Blackboard storage instead of return
        val discoveredTools = DiscoveredTools(
            tools = analyzedTools,
            domain = design.domain,
            discoveryTimestamp = Instant.now(),
            totalCandidatesAnalyzed = toolCandidates.size
        )
        
        // NEW: Store in blackboard for persistence
        blackboardManager.storeDiscoveredTools(discoveredTools)
        
        logger.info("✅ Discovered ${analyzedTools.size} tools for ${design.domain}")
        return discoveredTools
    }
    
    @AchievesGoal(description = "Analyze specific API for integration potential")
    @Action(cost = 0.4, value = 0.6)
    fun analyzeSpecificApi(apiUrl: String, context: AgentDesign): ApiAnalysis {
        // NEW: Triggered by shell command or GOAP planning
        logger.info("🔍 Analyzing API: $apiUrl")
        
        // UNCHANGED: Core analysis logic
        val analysis = apiIntrospectionEngine.analyzeOpenApiSpec(apiUrl)
        
        // NEW: Blackboard storage
        blackboardManager.storeApiAnalysis(apiUrl, analysis)
        
        return analysis
    }
    
    @Condition(name = "hasRelevantToolsForDomain", cost = 0.1)
    fun hasRelevantToolsForDomain(context: OperationContext): ConditionDetermination {
        // NEW: GOAP condition for planning optimization
        val currentDesign = blackboardManager.getCurrentDesign()
        val discoveredTools = blackboardManager.getDiscoveredTools()
        
        return ConditionDetermination(
            discoveredTools?.tools?.any { tool ->
                tool.relevanceScore > 0.7 && tool.integrationComplexity != IntegrationComplexity.VERY_HIGH
            } == true
        )
    }
}
```

## 🔧 **Integration with Existing Components**

### **1. Credential Management (Enhanced but Unchanged Core)**

```kotlin
/**
 * Human-in-the-loop credential management.
 * Core logic unchanged - now integrates with agent workflow.
 */
@Component
class CredentialManager {
    
    fun requestCredentials(tool: DiscoveredTool, context: AgentDesign): ToolCredentials {
        // UNCHANGED: Human-in-the-loop credential request
        val credentialRequest = CredentialRequest(
            toolName = tool.name,
            authScheme = tool.authenticationScheme,
            requiredScopes = extractRequiredScopes(tool, context),
            context = "Generating agent for ${context.domain}"
        )
        
        // UNCHANGED: Terminal-based credential input
        return terminalServices.requestCredentials(credentialRequest)
    }
    
    // NEW: Integration with agent actions
    fun getCredentialsForTool(toolName: String): ToolCredentials? {
        return credentialStore.get(toolName)
    }
}

// NEW: Shell command integration
@ShellComponent  
class MetaAgentShellCommands {
    
    @ShellMethod("Manage credentials for discovered tools")
    fun credentials(
        @ShellOption("--tool") toolName: String,
        @ShellOption("--action", defaultValue = "set") action: String
    ): String {
        return when (action) {
            "set" -> setCredentialsForTool(toolName)
            "list" -> listAvailableCredentials()
            "test" -> testToolCredentials(toolName)
            else -> "Unknown action: $action"
        }
    }
}
```

### **2. Knowledge Graph Construction (Unchanged Core)**

```kotlin
/**
 * Knowledge graph construction and maintenance.
 * Logic unchanged - now provides data to agent actions.
 */
@Component
class ToolKnowledgeGraphBuilder {
    
    fun buildFromApiRegistries(): KnowledgeGraph {
        // UNCHANGED: Crawl public API registries and container registries
        val apiRegistries = listOf(
            "https://api.apis.guru/v2/list.json", 
            "https://www.programmableweb.com/apis",
            "https://rapidapi.com/hub"
        )
        
        val containerRegistries = listOf(
            "https://hub.docker.com/v2/search/repositories/",
            "https://registry.hub.docker.com/v1/search",
            "https://quay.io/api/v1/repository"
        )
        
        // UNCHANGED: Extract API metadata
        val apiMetadata = apiRegistries.flatMap { registry ->
            crawlApiRegistry(registry)
        }
        
        // UNCHANGED: Extract containerized tool metadata
        val containerMetadata = containerRegistries.flatMap { registry ->
            crawlContainerRegistry(registry)
        }
        
        // Combine traditional APIs with containerized tools
        val allToolMetadata = apiMetadata + containerMetadata
        
        // UNCHANGED: Build semantic relationships for all tools
        val relationships = allToolMetadata.map { tool ->
            extractSemanticRelationships(tool)
        }.flatten()
        
        // UNCHANGED: Construct knowledge graph
        return KnowledgeGraph(
            nodes = allToolMetadata.map { it.toNode() },
            edges = relationships.map { it.toEdge() }
        )
    }
    
    // NEW: Periodic update triggered by agent
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // Daily
    fun updateKnowledgeGraph() {
        // Same logic but now can be triggered by meta-agent evolution
        val updatedGraph = buildFromApiRegistries()
        knowledgeGraphStore.update(updatedGraph)
    }
}
```

## 🎯 **GOAP Integration Benefits**

### **1. Intelligent Planning**

```kotlin
// Example GOAP planning sequence
Goal: Generate restaurant booking agent
├── Subgoal: Design agent architecture (cost=0.3, value=0.9)
│   └── Action: design(userInput) → AgentDesign
├── Subgoal: Discover relevant tools (cost=0.7, value=0.8)  
│   └── Action: discoverTools(design) → DiscoveredTools
│       ├── Analyze OpenTable API (cost=0.2)
│       ├── Analyze Yelp API (cost=0.2) 
│       └── Analyze Google Places API (cost=0.3)
├── Subgoal: Generate agent code (cost=0.5, value=1.0)
│   └── Action: generateAgent(design, tools) → GeneratedAgent
```

### **2. Type-Safe Action Chaining**

```kotlin
// Implicit conditions enable automatic chaining:

@Action
fun design(userInput: UserInput): AgentDesign
// Produces AgentDesign → creates implicit condition "AgentDesign available"

@Action  
fun discoverTools(design: AgentDesign): DiscoveredTools
// Consumes AgentDesign → requires implicit condition "AgentDesign available"
// GOAP automatically chains: design → discoverTools

@Action
fun generateAgent(design: AgentDesign, tools: DiscoveredTools): GeneratedAgent  
// Consumes both → requires both implicit conditions
// GOAP chains: design → discoverTools → generateAgent (optimal sequence)
```

### **3. Cost-Based Optimization**

```kotlin
// GOAP optimizes based on cost/value heuristics:

@Action(cost = 0.2, value = 0.6) // Low cost, medium value
fun analyzeSimpleRestApi(apiUrl: String): ApiAnalysis

@Action(cost = 0.8, value = 0.7) // High cost, high value  
fun analyzeComplexGraphQLApi(schemaUrl: String): GraphQLAnalysis

// GOAP prefers lower cost options when value is similar
// Automatically adjusts based on API complexity
```

## 🔄 **Blackboard State Management**

### **Tool Discovery State Flow**

```kotlin
class MetaAgentBlackboard(private val blackboard: Blackboard) {
    
    fun storeDiscoveredTools(tools: DiscoveredTools) {
        blackboard.store("discoveredTools", tools)
        blackboard.store("toolDiscoveryComplete", true)
        blackboard.store("toolDiscoveryTimestamp", Instant.now())
        
        // Store individual tool analyses for reference
        tools.tools.forEach { tool ->
            blackboard.store("tool_${tool.name}", tool)
        }
    }
    
    fun getToolsForDomain(domain: String): List<DiscoveredTool>? {
        return blackboard.get<DiscoveredTools>("discoveredTools")
            ?.tools
            ?.filter { it.domain == domain }
    }
    
    fun isToolDiscoveryComplete(): Boolean {
        return blackboard.get<Boolean>("toolDiscoveryComplete") == true
    }
}
```

### **Shell Integration**

```bash
# Shell workflow remains intuitive:
meta-agent:> design "Create a restaurant booking agent"
✅ Agent design stored on blackboard

meta-agent:> discoverTools --domain restaurants
🔍 Analyzing 15 restaurant APIs...
📡 Found: OpenTable, Yelp, Google Places, Resy
✅ Tools stored on blackboard

meta-agent:> bb
📋 Blackboard Contents:
├── currentDesign: RestaurantBookingAgent  
├── discoveredTools: [OpenTable, Yelp, Google Places, Resy]
├── toolDiscoveryComplete: true
└── toolDiscoveryTimestamp: 2024-01-15T10:30:00Z

meta-agent:> generate
⚙️ Using discovered tools for agent generation...
✅ Generated RestaurantBookingAgent.kt with 4 API integrations
```

## 📊 **Implementation Summary**

### **✅ What Remains Unchanged**:
- RAG vector similarity search algorithms
- Knowledge graph construction and traversal
- API introspection and schema analysis  
- Authentication scheme detection
- Integration complexity assessment
- Credential management workflows

### **🔄 What Gets Transformed**:
- **Service calls** → **Agent actions** with GOAP integration
- **Direct returns** → **Blackboard storage** with persistence
- **Manual orchestration** → **GOAP planning** with cost optimization
- **REST endpoints** → **Shell commands** with natural UX
- **Stateless operations** → **Stateful workflow** with history

### **🎯 Key Benefits**:
1. **GOAP Optimization**: Tool discovery cost balanced against value
2. **Type-Safe Chaining**: Automatic workflow orchestration
3. **Blackboard Persistence**: State maintained across shell sessions
4. **Error Recovery**: Failed discovery triggers alternative strategies
5. **Shell Integration**: Natural command-line workflow
6. **Planning Visibility**: Users see GOAP decision-making process

The tools discovery implementation preserves all the sophisticated RAG + Knowledge Graph intelligence while gaining the benefits of agent-native architecture with GOAP planning and persistent state management! 🚀