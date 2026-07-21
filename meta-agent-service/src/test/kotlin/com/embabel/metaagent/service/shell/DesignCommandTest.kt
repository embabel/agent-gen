/*
 * Copyright 2024-2025 Embabel Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.embabel.metaagent.service.shell

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class DesignCommandTest {

    @Test
    fun `both intent and spec-file returns error`() {
        val err = resolveDesignIntentError("some intent", "skills.md")
        assertThat(err).startsWith("❌").contains("not both")
    }

    @Test
    fun `neither intent nor spec-file returns error`() {
        val err = resolveDesignIntentError(null, null)
        assertThat(err).startsWith("❌").contains("Provide")
    }

    @Test
    fun `spec-file not found returns error with absolute path`(@TempDir tempDir: Path) {
        val missing = tempDir.resolve("missing.md").toString()
        assertThat(resolveDesignIntent(null, missing)).isNull()
        val err = resolveDesignIntentError(null, missing)
        assertThat(err).startsWith("❌").contains("not found").contains(missing)
    }

    @Test
    fun `spec-file absolute path is read`(@TempDir tempDir: Path) {
        val specFile = tempDir.resolve("spec.md").toFile()
        specFile.writeText("build a weather agent")
        val result = resolveDesignIntent(null, specFile.absolutePath)
        assertThat(result).isEqualTo("build a weather agent")
    }

    @Test
    fun `spec-file relative path resolves against CWD`(@TempDir tempDir: Path) {
        val originalUserDir = System.getProperty("user.dir")
        try {
            System.setProperty("user.dir", tempDir.toString())
            tempDir.resolve("skills.md").toFile().writeText("build a booking agent")
            val result = resolveDesignIntent(null, "skills.md")
            assertThat(result).isEqualTo("build a booking agent")
        } finally {
            System.setProperty("user.dir", originalUserDir)
        }
    }

    @Test
    fun `spec-file missing relative path includes resolved CWD in error`(@TempDir tempDir: Path) {
        val originalUserDir = System.getProperty("user.dir")
        try {
            System.setProperty("user.dir", tempDir.toString())
            assertThat(resolveDesignIntent(null, "nonexistent.md")).isNull()
            val err = resolveDesignIntentError(null, "nonexistent.md")
            assertThat(err).startsWith("❌ Spec file not found").contains(tempDir.toString())
        } finally {
            System.setProperty("user.dir", originalUserDir)
        }
    }

    @Test
    fun `inline intent is returned as-is`() {
        val result = resolveDesignIntent("find restaurants", null)
        assertThat(result).isEqualTo("find restaurants")
    }
}
