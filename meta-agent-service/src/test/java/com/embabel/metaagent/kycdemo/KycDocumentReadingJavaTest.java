package com.embabel.metaagent.kycdemo;

import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.rag.ingestion.TikaHierarchicalContentReader;
import com.embabel.agent.rag.service.support.DirectoryTextSearch;
import com.embabel.agent.rag.tools.ToolishRag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KycDocumentReadingJavaTest {

    @TempDir
    Path documentPackageDir;

    @Test
    void readKycDocumentPackageWithTikaAndExposeItThroughToolishRag() throws Exception {
        writeDocument("passport.txt", """
                Passport
                Name: John Smith
                Date of birth: 1983-01-12
                Nationality: GB
                Passport number: 123456789
                """);
        writeDocument("proof-of-address.txt", """
                Utility Bill
                Customer: John Smith
                Address: 10 Market Street, London, GB, SW1A 1AA
                """);
        writeDocument("certificate-of-incorporation.txt", """
                Certificate of Incorporation
                Company: Acme Payments Ltd
                Registration number: ACME-123
                Jurisdiction: GB
                Incorporated: 2020-01-15
                Director: John Smith
                """);
        writeDocument("shareholder-register.txt", """
                Shareholder Register
                Acme Payments Ltd
                John Smith owns 40 percent of ordinary shares.
                """);
        writeDocument("pep-declaration.txt", """
                PEP Declaration
                John Smith is a senior public figure.
                Public office: Deputy Minister of Finance
                Country: GB
                Current: true
                """);

        var reader = new TikaHierarchicalContentReader();
        var parsedDocuments = Files.list(documentPackageDir)
                .map(Path::toFile)
                .map(file -> reader.parseFile(file, file.toURI().toString()))
                .toList();

        assertTrue(parsedDocuments.stream().allMatch(document ->
                StreamSupport.stream(document.leaves().spliterator(), false).count() > 0
        ));

        var searchOperations = new DirectoryTextSearch(documentPackageDir.toString());
        var toolishRag = new ToolishRag(
                "kycDocuments",
                "KYC package documents for extraction into the KYC ontology",
                searchOperations
        );

        List<Tool> tools = toolishRag.tools();
        var textSearch = tools.stream()
                .filter(tool -> tool.getDefinition().getName().contains("textSearch"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No textSearch tool exposed. Tools: " +
                                tools.stream().map(tool -> tool.getDefinition().getName()).toList()
                ));

        var ownershipResult = contentOf(textSearch.call("""
                {"query":"John Smith owns ordinary shares","topK":5,"threshold":0.1}
                """));
        var spfResult = contentOf(textSearch.call("""
                {"query":"senior public figure public office","topK":5,"threshold":0.1}
                """));

        assertFalse(ownershipResult.isBlank());
        assertTrue(ownershipResult.contains("John Smith owns 40 percent"));
        assertFalse(spfResult.isBlank());
        assertTrue(spfResult.contains("senior public figure"));

        System.out.println("""
                ===== KYC DOCUMENT RAG =====
                Parsed documents: %d
                ToolishRag tools: %s

                Ownership search:
                %s

                SPF search:
                %s
                """.formatted(
                parsedDocuments.size(),
                tools.stream().map(tool -> tool.getDefinition().getName()).toList(),
                ownershipResult,
                spfResult
        ));
    }

    private void writeDocument(String fileName, String content) throws Exception {
        Files.writeString(documentPackageDir.resolve(fileName), content);
    }

    private String contentOf(Tool.Result result) {
        if (result instanceof Tool.Result.Text text) {
            return text.getContent();
        }
        return result.toString();
    }
}
