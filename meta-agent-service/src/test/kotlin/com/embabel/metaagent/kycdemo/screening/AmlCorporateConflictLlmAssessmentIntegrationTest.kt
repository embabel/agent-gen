package com.embabel.metaagent.kycdemo.screening

import com.embabel.agent.api.common.Ai
import com.embabel.metaagent.service.MetaAgentApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener

@SpringBootTest(classes = [MetaAgentApplication::class])
@ActiveProfiles("test")
@TestExecutionListeners(
    listeners = [DependencyInjectionTestExecutionListener::class],
    mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS,
)
class AmlCorporateConflictLlmAssessmentIntegrationTest {

    @Autowired
    lateinit var ai: Ai

    @Test
    @EnabledIfEnvironmentVariable(named = "AML_OFAC_SDN_XML", matches = ".+")
    fun `LLM applies logical rules to multiple possible OFAC corporate matches`() {
        val (_, assessment) = AmlCorporateConflictSupport.createAndScreenCorporateConflict()
        val ofacHits = assessment.hits.filter { it.source == ScreeningSource.OFAC_SANCTIONS }
        assertEquals(3, ofacHits.size)

        val candidateEvidence = ofacHits.joinToString("\n\n") { hit ->
            """
                Candidate:
                OFAC source record id: ${hit.sourceRecordId}
                OFAC primary name: ${hit.primaryName}
                Matched OFAC alias/name: ${hit.matchedName}
                Deterministic disposition: ${hit.disposition}
                Deterministic rationale: ${hit.rationale}
            """.trimIndent()
        }

        val prompt = """
            You are assisting an AML analyst with case disposition for sanctions screening hits.

            Apply these logical rules. Do not use numeric weights:
            - A near-exact legal name or alias match is material and must not be ignored.
            - A sanctions-list alias match can justify a possible match even when the primary list name differs.
            - Missing company registration-number agreement prevents confirming the match.
            - Conflicting jurisdiction or country evidence lowers confidence.
            - Do not recommend CONFIRMED_MATCH unless strong secondary identifiers agree.
            - Do not recommend LIKELY_FALSE_POSITIVE when the name or alias evidence is very strong and
              the only problems are missing or conflicting secondary evidence.
            - Use MEDIUM confidence when name evidence is strong but secondary identifiers are missing or conflicting.
            - Assess each candidate separately because the KYC subject may or may not be the same entity as any
              individual sanctions candidate.
            - Compute confidenceScore yourself as a number from 0.0 to 1.0 using the logical evidence.
            - ConfidenceScore is an LLM analyst estimate, not a deterministic matcher score.
            - Use higher confidence when the matched name is the primary legal name or strong alias and jurisdiction
              is compatible; use lower confidence when only a broad group alias matches or secondary identifiers conflict.
            - Return exactly one candidate assessment for each candidate listed below.

            Candidate evidence:
            Subject legal name: ${assessment.subject.displayName}
            Subject jurisdiction: ${assessment.subject.countries.joinToString()}
            Subject registration number: ${assessment.subject.identifiers.joinToString { it.value }}
            Overall deterministic status: ${assessment.status}

            $candidateEvidence

            Return a structured subject-level assessment with:
            subjectName, overallStatus, candidateAssessments, rationale, analystAction.
            Each candidate assessment must include:
            sourceRecordId, candidateName, confidenceScore, confidenceLevel, recommendedDisposition, rationale, missingEvidence.
        """.trimIndent()

        val llmAssessment = ai.withDefaultLlm()
            .creating(AmlLogicalScreeningAssessment::class.java)
            .withExample(
                "Possible corporate sanctions matches",
                AmlCorporateConflictSupport.genericPossibleCorporateScreeningExample(),
            )
            .fromPrompt(prompt)

        assertEquals(ScreeningAssessmentStatus.MANUAL_REVIEW_REQUIRED, llmAssessment.overallStatus)
        assertEquals(3, llmAssessment.candidateAssessments.size)
        assertEquals(setOf("23648", "23652", "23653"), llmAssessment.candidateAssessments.map { it.sourceRecordId }.toSet())
        assertTrue(llmAssessment.candidateAssessments.all { it.confidenceLevel == AmlLlmConfidenceLevel.MEDIUM })
        assertTrue(llmAssessment.candidateAssessments.all { it.recommendedDisposition == ScreeningDisposition.POSSIBLE_MATCH })
        assertTrue(llmAssessment.candidateAssessments.all { it.confidenceScore in 0.0..1.0 })
        assertTrue(llmAssessment.candidateAssessments.all { it.missingEvidence.isNotEmpty() })

        logger.info(
            "LLM corporate AML conflict assessment: subject={}, status={}, candidates={}, action={}, rationale={}",
            llmAssessment.subjectName,
            llmAssessment.overallStatus,
            llmAssessment.candidateAssessments.joinToString { candidate ->
                "recordId=${candidate.sourceRecordId}, candidate='${candidate.candidateName}', " +
                    "confidence=${candidate.confidenceScore}/${candidate.confidenceLevel}, " +
                    "disposition=${candidate.recommendedDisposition}, missingEvidence=${candidate.missingEvidence}, " +
                    "rationale=${candidate.rationale}"
            },
            llmAssessment.analystAction,
            llmAssessment.rationale,
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AmlCorporateConflictLlmAssessmentIntegrationTest::class.java)

        @BeforeAll
        @JvmStatic
        fun setUp() {
            System.setProperty("embabel.agent.shell.interactive.enabled", "false")
        }
    }
}
