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
- `AgentContext`: agent generation context data class
- `ToolRegistry`: dynamic tool discovery
- Prompt templates and utilities

Use this module in other Kotlin applications or libraries.

---

### 🟢 `meta-agent-service`
> **Spring Boot REST API**

Features:
- REST endpoint to accept JSON and return generated code
- Configurable via `application.yml` (`OpenAI` or other providers)
- Can be deployed as a microservice

Example API call:

POST /generate
Content-Type: application/json
```
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
```
