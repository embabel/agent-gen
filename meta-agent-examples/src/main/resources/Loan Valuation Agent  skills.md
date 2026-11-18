# Loan Valuation Agent

## Purpose

The Loan Valuation Agent produces a reproducible and auditable valuation of a private/personal loan portfolio.

The agent uses **Embabel for deterministic orchestration**, **LLMs only for semantic understanding and report preparation**, and **Forge for quantitative valuation and risk calculations**.

The agent MUST NOT use an LLM to calculate valuations, sensitivities, cash flows, or other quantitative results.

## Core Principles

1. **LLM → Understand and Explain**
   - Extract structured facts from unstructured documents.
   - Summarize validated valuation results.
   - Prepare valuation narrative.

2. **Embabel → Validate, Decide, and Orchestrate**
   - Maintain typed portfolio state.
   - Apply deterministic validation rules.
   - Classify loans using explicit rules.
   - Select valid execution paths.
   - Enforce action preconditions and postconditions.
   - Maintain provenance and audit information.

3. **Forge → Calculate**
   - Generate valuation cash flows.
   - Calculate loan and portfolio valuations.
   - Model default/recovery and restructuring.
   - Calculate sensitivities and risk measures.

LLM-generated information MUST be converted into typed domain objects and validated before it can be used by Forge.

---

# Inputs

The agent accepts two complementary forms of input.

### Structured portfolio feed

Examples:

- CSV
- JSON
- API payload

Typical fields include:

- loan identifier
- outstanding principal
- interest rate
- rate type
- payment frequency
- origination date
- maturity date
- currency
- borrower information
- payment status
- delinquency information
- default status
- restructuring indicators
- collateral information, where applicable

### Supporting documents

Examples:

- loan brochures
- term sheets
- loan agreements
- restructuring agreements
- workout documents
- borrower notices
- other valuation-relevant documentation

Structured feeds and documents MAY describe the same loan.

The agent MUST preserve the source and provenance of material information.

---

# Canonical Domain State

The ingestion process produces a canonical typed portfolio representation.

Conceptually:

```text
LoanPortfolio
    |
    +-- Loan
    |    +-- LoanTerms
    |    +-- LoanStatus
    |    +-- RestructuringTerms?
    |    +-- DefaultInformation?
    |    +-- CollateralInformation?
    |    +-- SourceProvenance
    |
    +-- ...
```

Values extracted from documents MUST NOT silently override structured-feed values.

Material inconsistencies MUST be identified during validation.

---

# Loan States

For Phase 1, the minimum valuation-relevant states are:

```text
PERFORMING
DEFAULTED
RESTRUCTURED
```

Additional states such as `DELINQUENT` MAY be introduced when they produce a materially different valuation path.

Loan-state classification MUST be deterministic.

The LLM MAY extract facts indicating default or restructuring, but MUST NOT make the authoritative loan-state decision.

For example:

```text
Restructuring agreement
        |
        | LLM extraction
        v
paymentHoliday = 6 months
maturityExtended = true
interestRateModified = true
        |
        | deterministic classification
        v
RESTRUCTURED
```

---

# Agent Actions

## 1. IngestPortfolio

**Purpose:**  
Load the structured portfolio feed and associated supporting documents.

**Input:**

```text
PortfolioValuationRequest
```

**Output:**

```text
RawPortfolioData
```

**Responsibilities:**

- identify portfolio and valuation date;
- load structured loan records;
- associate available documents with loans;
- establish source provenance;
- reject unreadable or unsupported input.

**LLM:** No.

**Forge:** No.

---

## 2. ExtractDocumentData

**Purpose:**  
Convert relevant unstructured loan documentation into typed facts.

**Input:**

```text
RawPortfolioData
```

**Output:**

```text
ExtractedLoanData
```

**Responsibilities:**

Use an LLM where semantic document interpretation is required.

Extract information such as:

- contractual loan terms;
- interest-rate provisions;
- maturity;
- repayment conditions;
- default information;
- payment holidays;
- modified terms;
- restructuring provisions;
- recovery/workout information;
- collateral information.

Every material extracted value SHOULD retain source provenance.

Example:

```text
value: 5.50%
field: interestRate
source: restructuring-agreement.pdf
page: 7
```

The LLM MUST NOT calculate valuation values.

**LLM:** Yes.

**Forge:** No.

---

## 3. ValidatePortfolioData

**Purpose:**  
Determine whether portfolio and loan information is sufficiently complete and internally consistent for valuation.

**Input:**

```text
RawPortfolioData
ExtractedLoanData
```

**Output:**

```text
ValidatedLoanPortfolio
```

**Responsibilities:**

Perform deterministic checks including:

- required fields;
- valid dates;
- valid balances;
- valid interest rates;
- maturity relative to valuation date;
- currency consistency;
- payment-schedule consistency;
- required default/restructuring information;
- feed-versus-document consistency.

When feed and document values conflict, the agent MUST NOT silently choose a value unless an explicit deterministic precedence rule exists.

Material unresolved conflicts MUST be recorded as exceptions.

**LLM:** No.

**Forge:** No.

---

## 4. ClassifyLoans

**Purpose:**  
Determine the valuation-relevant state of each validated loan.

**Input:**

```text
ValidatedLoanPortfolio
```

**Output:**

```text
ClassifiedLoanPortfolio
```

**Minimum classifications:**

```text
PERFORMING
DEFAULTED
RESTRUCTURED
```

Classification MUST use deterministic rules based on validated facts.

Example:

```text
if restructuring criteria satisfied
    -> RESTRUCTURED
else if default criteria satisfied
    -> DEFAULTED
else
    -> PERFORMING
```

**LLM:** No.

**Forge:** No.

---

## 5. PrepareValuationInputs

**Purpose:**  
Transform validated and classified loans into Forge-compatible valuation inputs.

**Input:**

```text
ClassifiedLoanPortfolio
MarketData
ValuationAssumptions
```

**Output:**

```text
PortfolioValuationInputs
```

The preparation logic MUST account for the loan state.

### Performing

Prepare contractual cash-flow and discounting inputs.

### Defaulted

Prepare recovery/workout inputs and applicable expected cash flows.

### Restructured

Construct inputs using the modified contractual terms.

This action MUST NOT perform the quantitative valuation itself.

**LLM:** No.

**Forge:** No.

---

## 6. CalculatePortfolioValuation

**Purpose:**  
Calculate the quantitative valuation using Forge.

**Input:**

```text
PortfolioValuationInputs
```

**Output:**

```text
PortfolioValuation
```

**Forge:** Yes.

**LLM:** No.

Two execution strategies MAY be supported.

### Strategy A — Embabel parallel execution

```text
Loan 1 ------> Forge
Loan 2 ------> Forge
Loan 3 ------> Forge
...
Loan N ------> Forge
```

Embabel invokes independent loan valuation operations concurrently and gathers the results.

### Strategy B — Forge bulk execution

```text
Portfolio
    |
    v
Forge Bulk Valuation
    |
    +-- Loan 1
    +-- Loan 2
    +-- ...
    +-- Loan N
```

Forge owns portfolio partitioning and parallel execution.

The business semantics of `CalculatePortfolioValuation` MUST remain independent of the selected execution strategy.

---

## 7. CalculatePortfolioSensitivities

**Purpose:**  
Calculate the minimum Phase-1 portfolio sensitivity and risk measures.

**Input:**

```text
PortfolioValuationInputs
PortfolioValuation
```

**Output:**

```text
PortfolioSensitivities
```

The exact Phase-1 sensitivity set SHOULD be explicitly configured.

Possible examples include:

- interest-rate sensitivity;
- spread sensitivity;
- recovery-rate sensitivity;
- default-related sensitivity;
- restructuring-related sensitivity.

All quantitative calculations MUST be performed by Forge.

**LLM:** No.

**Forge:** Yes.

More computationally intensive portfolio risk models belong to Phase 2.

---

## 8. ValidateValuationResults

**Purpose:**  
Apply deterministic reasonableness controls to calculated results.

**Input:**

```text
PortfolioValuation
PortfolioSensitivities
```

**Output:**

```text
ValidatedPortfolioValuation
```

Controls MAY include:

- missing valuation results;
- failed Forge calculations;
- unexpected negative or extreme values;
- tolerance breaches;
- valuation movements exceeding configured thresholds;
- sensitivity anomalies;
- default/restructuring inconsistencies;
- portfolio aggregation checks.

Failures MUST result in an explicit exception or escalation state.

They MUST NOT be hidden or corrected by an LLM.

**LLM:** No.

**Forge:** No, except where a configured challenger calculation is explicitly required.

---

## 9. PrepareValuationReport

**Purpose:**  
Produce the human-readable explanation of the validated valuation.

**Input:**

```text
ValidatedLoanPortfolio
ValidatedPortfolioValuation
SourceProvenance
ValuationExceptions
```

**Output:**

```text
ValuationReport
```

The LLM MAY generate:

- executive portfolio summary;
- valuation methodology narrative;
- description of material assumptions;
- default summary;
- restructuring summary;
- sensitivity summary;
- exception summary;
- explanations of material valuation movements.

The LLM MUST use validated typed information as its factual source.

The LLM MUST NOT:

- recalculate numbers;
- modify Forge results;
- invent missing values;
- resolve unresolved valuation exceptions;
- present unsupported claims as facts.

Material quantitative values SHOULD be inserted directly from typed valuation objects wherever practical.

**LLM:** Yes.

**Forge:** No.

---

## 10. AssembleValuationPackage

**Purpose:**  
Assemble the final reproducible valuation deliverable.

**Input:**

```text
ValidatedPortfolioValuation
PortfolioSensitivities
ValuationReport
SourceProvenance
AuditRecord
```

**Output:**

```text
PortfolioValuationPackage
```

The package SHOULD contain:

- portfolio valuation;
- position-level valuations;
- sensitivities;
- methodology;
- assumptions;
- default/restructuring information;
- material exceptions;
- source provenance;
- validation results;
- execution/audit information;
- generated valuation report.

**LLM:** No.

**Forge:** No.

---

# Action Flow

```text
IngestPortfolio
       |
       v
ExtractDocumentData
       |  LLM
       v
ValidatePortfolioData
       |
       v
ClassifyLoans
       |
       v
PrepareValuationInputs
       |
       v
CalculatePortfolioValuation
       |  Forge
       v
CalculatePortfolioSensitivities
       |  Forge
       v
ValidateValuationResults
       |
       v
PrepareValuationReport
       |  LLM
       v
AssembleValuationPackage
```

The planner MAY select different execution paths based on portfolio state, validation results, loan states, exceptions, and available information.

---

# LLM Boundary

The LLM has two primary responsibilities.

## Understand

```text
Unstructured documents
        |
        v
       LLM
        |
        v
Typed loan facts
        |
        v
Deterministic validation
```

## Explain

```text
Validated valuation results
        |
        v
       LLM
        |
        v
Valuation narrative
```

The LLM is NOT responsible for:

```text
loan classification
valuation
cash-flow mathematics
sensitivity calculations
reasonableness controls
portfolio aggregation
execution planning
```

---

# Forge Boundary

Forge is the authoritative quantitative engine.

Embabel MUST invoke Forge through a typed contract.

For Phase 1, Forge MAY be exposed as:

- a typed REST service;
- a typed RPC service;
- an MCP tool/service if MCP provides operational benefits.

The business action MUST remain independent of transport.

For long-running Phase-2 HPC calculations, synchronous tool semantics SHOULD NOT be assumed. An asynchronous:

```text
submit -> jobId -> status -> result
```

contract MAY be required.

---

# Persistence and Audit

The agent MUST persist sufficient information to reproduce and explain a valuation.

The store SHOULD include:

- canonical portfolio state;
- source provenance;
- extracted document facts;
- validation results;
- loan classifications;
- valuation inputs;
- Forge requests/results;
- sensitivity results;
- exceptions;
- action execution records;
- report outputs.

Audit records SHOULD identify the action, inputs, outputs, execution status, model/tool used where applicable, and relevant timestamps.

---

# Phase 1 Scope

Phase 1 includes:

- structured portfolio ingestion;
- document ingestion;
- LLM-assisted extraction;
- deterministic validation;
- performing/defaulted/restructured loan handling;
- deterministic Forge valuation;
- minimum sensitivities/risk;
- reasonableness controls;
- LLM-assisted report preparation;
- auditable valuation package.

# Phase 2 Scope

Phase 2 MAY add:

- portfolio Monte Carlo;
- correlated defaults;
- tail-risk modelling;
- copulas;
- EVT;
- deep-BSDE methods;
- GPU execution;
- SLURM/HPC job orchestration;
- advanced challenger models.

These capabilities MUST extend the quantitative Forge layer without moving numerical computation into the LLM.