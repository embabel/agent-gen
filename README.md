# 🧠 Meta-Agent

Meta-Agent is a Kotlin framework for **generative AI agent creation**. It uses Large Language Models (LLMs) to dynamically generate Kotlin agent code from natural language prompts and structured Model Context Protocol (MCP) inputs. Generated Agent code employs Embabel Agent APIs.

---

## ✨ Features

✅ Prompt-driven agent code generation using Embabel Agent APIs
✅ MCP (Model Context Protocol) structured input  
✅ Dynamic tool discovery  
✅ Spring Boot REST API server  
✅ Example projects and demos  

---

## 🧩 Project Structure

This is a **multi-module Gradle project**:

meta-agent/
├── meta-agent-core/ # Core framework: LLM abstractions, MCP classes, code generation logic
├── meta-agent-service/ # Spring Boot app: REST API for code generation
├── meta-agent-examples/ # Example agents, generated code samples, usage tutorials


---

### 🟢 `meta-agent-core`
> **Reusable framework module**

Contains:
- `MetaAgent`: main class for prompt-driven code generation
- `LLMClient`: pluggable interface to call any LLM provider thru Embabel Agent API
- `AgentContext`: MCP data class
- `ToolRegistry`: dynamic tool discovery
- Prompt templates and utilities

Use this module **standalone** in other Kotlin applications or libraries.

---

### 🟢 `meta-agent-service`
> **Spring Boot REST API**

Features:
- REST endpoint to accept MCP JSON and return generated code
- Configurable via `application.yml` (`OpenAI` or other providers)
- Can be deployed as a microservice

Example API call:

POST /generate
Content-Type: application/json

{
"task": "restaurant_reservation",
"location": "Berlin",
"preferences": {
"cuisine": "vegan"
},
"toolsAvailable": ["OpenTableAPI"]
}

val llmClient = OpenAILLMClient(apiKey = "sk-...")
val metaAgent = MetaAgent(llmClient)

val context = AgentContext(
    task = "restaurant_reservation",
    location = "Berlin",
    preferences = mapOf("cuisine" to "Italian"),
    toolsAvailable = listOf("OpenTableAPI", "HEREAPI")
)

val generatedCode = metaAgent.generateAgentCodeFromContext(context)
println(generatedCode)

