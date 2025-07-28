# Configuration Migration Plan

## Overview
All hardcoded values in MetaAgent should be moved to configuration files for flexibility and customization.

## Hardcoded Values to Move to Configuration

### 1. Action Verb Lists
**Current Location**: `MetaAgent.extractActionIntentsFromContent()`
```kotlin
val actionVerbs = listOf(
    "search", "find", "get", "retrieve", "fetch", "collect", "gather",
    "send", "notify", "alert", "inform", "deliver", "transmit",
    "process", "handle", "manage", "execute", "perform", "run",
    "create", "make", "build", "generate", "produce", "construct",
    "update", "modify", "change", "edit", "save", "store",
    "analyze", "calculate", "compute", "determine", "evaluate",
    "validate", "check", "verify", "confirm", "ensure"
)
```
**Should Move To**: `config/action-verbs.yml`

### 2. Goal Words
**Current Location**: `MetaAgent.inferGoalsFromIntent()`
```kotlin
val goalWords = listOf("complete", "finish", "achieve", "provide", "ensure", "deliver", "fulfill")
```
**Should Move To**: `config/goal-words.yml`

### 3. Stop Words (Articles/Prepositions)
**Current Location**: Multiple methods
```kotlin
// In extractActionIntentsFromContent()
candidateNoun !in listOf("the", "and", "for", "with", "that", "this", "from", "into", "onto")

// In findDomainNounFromContent()
word !in listOf("agent", "system", "service", "application", "create", "build", "help", "user")
```
**Should Move To**: `config/stop-words.yml`

### 4. Package Name Template
**Current Location**: `MetaAgent.generatePackageName()`
```kotlin
return "com.embabel.generated.$cleanDomain"
```
**Should Move To**: `config/agent-config.properties`
```properties
agent.package.template=com.embabel.generated.{domain}
```

### 5. GOAP Heuristics Defaults
**Current Location**: `MetaAgent.generateGoapHeuristics()`
```kotlin
return mapOf(
    "primaryActionCost" to 0.3,
    "primaryActionValue" to 0.8,
    "secondaryActionCost" to 0.5,
    "secondaryActionValue" to 0.6
)
```
**Should Move To**: `config/goap-defaults.yml`

### 6. Domain Inference Stop Words
**Current Location**: `MetaAgent.createFallbackSpecification()`
```kotlin
word !in listOf("create", "agent", "for", "the", "and", "with", "that", "help", "make")
```
**Should Move To**: `config/domain-stop-words.yml`

### 7. Agent Name Stop Words
**Current Location**: `MetaAgent.inferAgentName()`
```kotlin
word !in listOf("agent", "create", "help", "user", "system", "make", "with", "that", "this")
```
**Should Move To**: `config/naming-stop-words.yml`

## Proposed Configuration Structure

```
src/main/resources/config/
├── action-verbs.yml           # Action verb vocabulary
├── goal-words.yml             # Goal/outcome vocabulary  
├── stop-words.yml             # General stop words
├── domain-stop-words.yml      # Domain extraction stop words
├── naming-stop-words.yml      # Agent naming stop words
├── goap-defaults.yml          # GOAP heuristics defaults
└── agent-config.properties    # General agent configuration
```

## Example Configuration Files

### `config/action-verbs.yml`
```yaml
action_verbs:
  retrieval: ["search", "find", "get", "retrieve", "fetch", "collect", "gather"]
  communication: ["send", "notify", "alert", "inform", "deliver", "transmit"]
  processing: ["process", "handle", "manage", "execute", "perform", "run"]
  creation: ["create", "make", "build", "generate", "produce", "construct"]
  modification: ["update", "modify", "change", "edit", "save", "store"]
  analysis: ["analyze", "calculate", "compute", "determine", "evaluate"]
  validation: ["validate", "check", "verify", "confirm", "ensure"]
```

### `config/agent-config.properties`
```properties
# Package naming
agent.package.template=com.embabel.generated.{domain}
agent.name.suffix=Agent

# Text processing
text.min.word.length=3
text.min.domain.word.length=4
text.max.action.intents=5
text.max.goal.intents=3

# Fallback behavior
fallback.use.smart.extraction=true
fallback.use.domain.nouns=true
```

### `config/goap-defaults.yml`
```yaml
goap_heuristics:
  action_defaults:
    primary_cost: 0.3
    primary_value: 0.8
    secondary_cost: 0.5
    secondary_value: 0.6
  planning:
    max_actions: 10
    max_goals: 5
```

## Implementation Strategy

1. **Phase 1**: Create configuration file structure
2. **Phase 2**: Create configuration loader service
3. **Phase 3**: Migrate hardcoded values method by method
4. **Phase 4**: Add configuration validation and defaults
5. **Phase 5**: Document configuration options for users

## Benefits

- **Customizable**: Users can adapt MetaAgent for different domains/languages
- **Maintainable**: Central location for all vocabulary and rules
- **Testable**: Different configurations for different test scenarios
- **Extensible**: Easy to add new verb categories or languages
- **Domain-Specific**: Users can create domain-specific vocabularies