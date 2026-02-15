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

**🟢 Current Status**: **Core Functionality Complete**

### Achieved
- ✅ **Agent Generation** - `design` command generates `@Agent`, `@Action`, `@AchievesGoal` annotated code
- ✅ **Provider Discovery (Test)** - Discovers API providers (Foursquare, OpenTable, etc.) with documentation URLs
- ✅ **Restaurant Finder Pipeline** - E2E test: Foursquare → Brave Search → Jsoup → LLM comparison
- ✅ **Agent-native recursive architecture** - MetaAgent IS an `@Agent` using same patterns it generates

### Realistic Automation Scope

| Automated                          | Manual (with guidance) |
|------------------------------------|------------------------|
| Agent skeleton generation          | Get API tokens |
| Provider discovery Advisory        | Implement tool logic |
| Tool skeleton generation (planned) | Wire tools into agent |

**Key Insight**: Full automation from prompt → running agent is unrealistic. Meta-Agent provides scaffolding, discovery, and guidance. Humans provide credentials and domain-specific implementation.


**Key Innovation: Agent-Native Recursive Architecture** - The meta-agent IS an agent, using identical patterns to what it generates, creating unprecedented architectural consistency and self-improvement capabilities.

## 🏗️ Architecture Summary

**🏗️ Component Relationship:**
```
+---------------------------------------------------------------+
|                      meta-agent-service                       |
|                      (Spring Boot App)                        |
|                                                               |
|  +-------------------+   calls   +---------------------+      |
|  |    Shell App      |---------->|  MetaAgentService   |      |
|  |  (CLI Interface)  |           |  (Business Logic)   |      |
|  +-------------------+           +---------------------+      |
|                                           |                   |
|                                           | calls             |
|                                           v                   |
+---------------------------------------------------------------+
                                            |
                                            | calls
                                            v
+---------------------------------------------------------------+
|                       meta-agent-core                         |
|                                                               |
|  +---------------------+       +---------------------+        |
|  |     MetaAgent       |       |   Data Models &     |        |
|  |    (Core Engine)    |       |     Interfaces      |        |
|  |                     |       |                     |        |
|  |  - LLM Processing   |       |  - MetaAgentContext |        |
|  |  - Tool Discovery   |       |  - GeneratedAgent   |        |
|  |  - Code Generation  |       |  - AuditFramework   |        |
|  +---------------------+       +---------------------+        |
+---------------------------------------------------------------+
```

**🔄 Execution Flow:**
1. User runs shell command (e.g., `generate "restaurant booking agent"`)
2. **Shell App** creates MetaAgentContext with user input
3. **Shell App** calls `MetaAgentService.generateAgent(context)`
4. **MetaAgentService** calls `MetaAgent.generateAgent(context)`
5. **MetaAgent** orchestrates LLM, templates, validation, etc.
6. Generated agent flows back: **MetaAgent** → **MetaAgentService** → **Shell App** → **User**

---

## 🛠️ Practical Guide - Agent Generation Steps

### Flow Diagram

```
+-----------------------------------------------------------------------+
|                      AGENT GENERATION WORKFLOW                        |
+-----------------------------------------------------------------------+

                                    |
                                    v
+-----------------------------------------------------------------------+
| 1. Generate Agent                                                     |
|    Commands: design, gen-agent                                        |
+-----------------------------------------------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
| 2. Generate Tool Skeleton                                             |
|    Command: gen-tools                                                 |
+-----------------------------------------------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
| 3. Provider Discovery (Advisory)                                      |
|    Adjust for domain: ProviderDiscoveryService                        |
+-----------------------------------------------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
| 4. Tool Discovery                                                     |
|    Adjust for domain: ToolsDiscoveryIntegrationTest                   |
|    - Validates API connectivity                                       |
|    - Tests endpoint accessibility                                     |
+-----------------------------------------------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
| 5. Manual Tool Wiring                                                 |
|    Implement tool methods with actual API calls                       |
+-----------------------------------------------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
| 6. Wire Tools in Agent                                                |
|    - Connect tools to agent actions                                   |
|    - Configure @LlmTool and @Action annotations                       |
+-----------------------------------------------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
|                         WORKING AGENT                                 |
|                    with tools, ready for testing                      |
+-----------------------------------------------------------------------+
```

### Step-by-Step Instructions

| Step | Command/Action | Output | Reference |
|------|----------------|--------|-----------|
| **1. Generate Agent** | `design` → `gen-agent` | `@Agent` annotated Kotlin class | `MetaAgentTest` |
| **2. Generate Tool Skeleton** | `gen-tools` | `@LlmTool` annotated tools class | - |
| **3. Provider Discovery** | Adjust for domain | List of API providers with access models | `ProviderDiscoveryServiceTest` |
| **4. Tool Discovery** | Adjust for domain | Verified API endpoints | `ToolsDiscoveryIntegrationTest` |
| **5. Manual Tool Wiring** | Implement tool methods | Working API calls (FourSquare, Brave, etc.) | `RestaurantFinderTools` |
| **6. Wire Tools in Agent** | Connect tools to actions | Complete agent with tool integration | `RestaurantFinder` |

### Key Files for Reference

- **Agent Example**: `meta-agent-examples/.../RestaurantFinder.kt`
- **Tools Example**: `meta-agent-examples/.../RestaurantFinderTools.kt`
- **Pipeline Test**: `RestaurantFinderPipelineExtensionTest.kt` (Foursquare → Brave → Jsoup → LLM)

---


### _Ideal_ Tool Discovery Flow

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


## 🔧 Development Pipeline for Generated Agents

### Generated Agent Lifecycle

```
Agent Generation → Workspace Creation → Compile → Test → Package → Deploy
```
## ** SampleWorkspace Development** 
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



### **Generated Agent Configuration - TODO**

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

#### **Generated Agent Logging Theme Implementation* - NICE TO HAVE*


#### **User-Defined Resource Files - NICE TO HAVE**

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




## 📚 Code Inventory

### Test Files

| Test File | Description |
|-----------|-------------|
| `AgentSpecificationTest.kt` | Unit tests for AgentSpecification data class: Jakarta validation constraints, package name generation, iterative specification gathering, finalization process, immutability, and thread safety |
| `MetaAgentTest.kt` | Unit tests for MetaAgent core: agent design generation from UserInput, GOAP goal vs capability distinction, annotation validation (@Agent, @Action, @AchievesGoal), cost/value heuristics, and fallback behavior |
| `LlmApiExtractorTest.kt` | Integration test for LLM-based API extraction: extracts API endpoints from documentation URLs (OpenTable, Yelp) using two methods - direct URL extraction and document parsing |
| `BaseUrlExtractorTest.kt` | Integration test for base URL extraction: discovers production/sandbox/test/demo URLs from API documentation pages (OpenTable, Yelp) |
| `ApiVerificationHarnessTest.kt` | Integration test for API verification: combines LLM extraction + base URL extraction + HTTP testing to verify real endpoint accessibility, categorizes as PUBLICLY_ACCESSIBLE, REQUIRES_AUTH, NOT_ACCESSIBLE |
| `ToolsDiscoveryIntegrationTest.kt` | E2E test for ToolsDiscoveryService: discovers API tools for domains (restaurant, weather), tests real API connectivity via HTTP HEAD requests (Yelp, Resy, OpenTable), validates 3-stage flow (discover → authenticate → test), scores tools for specific actions |
| `ProviderDiscoveryServiceTest.kt` | Tests ProviderDiscoveryService (Stage 1A): discovers API providers by name/capabilities, tests UrlDiscoveryService for documentation URLs, tests UrlVerificationService for reachability, validates automation-first approach (PUBLIC_SIGNUP vs PARTNER_ONLY access models) |
| `RestaurantFinderPipelineTest.kt` | Full pipeline integration test: validates framework with "finder" use case targeting free-tier APIs, iterates through ranked providers until accessible API found, demonstrates prod classes work unchanged with different AgentSpecification inputs |
| `RestaurantFinderPipelineExtensionTest.kt` | Step-by-step pipeline: (1) Foursquare search → restaurant URLs, (2) Brave Search → menu URLs, (3) Jsoup → JSON-LD extraction, (4) LLM with @LlmTool → menu comparison. Includes MenuTools class pattern for tool creation |
| `RestaurantFinderParallelToolLoopTest.kt` | Parallel tool execution test: enables ParallelToolLoop, creates dynamically-named tools per restaurant using Tool.of(), demonstrates concurrent menu fetching pattern with thread logging |

### Generated Code (meta-agent-examples)

| File | Description |
|------|-------------|
| `RestaurantFinder.kt` | GOAP agent with 3 actions: findRestaurants (UserInput→NearbyRestaurants), getMenus (NearbyRestaurants→RestaurantMenus), compareMenus (RestaurantMenus→MenuComparisonResult). Uses NLP extraction for query/location, invokes LLM withTools for menu comparison |
| `RestaurantFinderTools.kt` | 3 tools: (1) findRestaurantUrls - FourSquare API with Bearer auth, (2) getMenuUrls - Brave Search for allmenus.com URLs, (3) readMenuAsJson - Jsoup JSON-LD extraction. Tools 1&2 called directly by agent, Tool 3 invoked via LLM |

### Core Services (meta-agent-core)

| Service | Description |
|---------|-------------|
| `MetaAgent.kt` | Core meta-agent: @Agent with GOAP planner, createAgentSpecification extracts domain/actions/goals from UserInput, generateAgent produces annotated Kotlin code |
| `agentToFileWriter.kt` | Writes generated code to `meta-agent-examples/src/main/generated/kotlin/{package}/` with proper Maven structure |
| `ToolsDiscoveryService` | Discovers DiscoveredTool objects with apiUrl, apiType, authenticationScheme, integrationComplexity, categories |
| `ProviderDiscoveryService` | Discovers ProviderCandidate with name, capabilities, accessModel (PUBLIC_SIGNUP/APPROVAL_REQUIRED/PARTNER_ONLY), targetMarket |
| `UrlDiscoveryService` | Finds documentation URLs for providers with confidence scores |
| `UrlVerificationService` | HTTP verification of URLs, returns VerifiedUrl with status (REACHABLE/NEEDS_AUTH/NOT_FOUND/ERROR) and adjustedConfidence |
| `ApiAuthenticationAnalyzer` | Analyzes API authentication requirements and schemes |

### Shell Commands (meta-agent-service)

| Command | Description |
|---------|-------------|
| `design` | Generate AgentSpecification from natural language, stores in blackboard |
| `gen-agent` | Generate @Agent annotated Kotlin code from AgentSpecification on blackboard |
| `gen-tools` | Generate @LlmTool annotated tools skeleton from AgentSpecification on blackboard |

---


