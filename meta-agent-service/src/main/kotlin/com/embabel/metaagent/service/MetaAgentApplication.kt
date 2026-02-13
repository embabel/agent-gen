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
package com.embabel.metaagent.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import com.embabel.agent.config.annotation.LoggingThemes

/**
 * Spring Boot application that runs the Meta-Agent framework in interactive shell mode.
 *
 * This application provides a command-line interface for generating and managing agents
 * through the Meta-Agent framework. The shell allows executing meta-agent commands,
 * agent generation workflows, and debugging agent generation processes.
 *
 * ## Example Usage
 * ```
 * shell:> design "Create an agent that processes customer orders"
 * shell:> generate
 * shell:> test
 * shell:> help
 * ```
 *

 */
@SpringBootApplication

@ComponentScan(basePackages = ["com.embabel.metaagent", "com.embabel.agent"])
class MetaAgentApplication {
    companion object {
        /**
         * Application entry point that bootstraps the Spring Boot application.
         *
         * Initializes the Spring context with agent auto-configuration and
         * starts the interactive shell interface for meta-agent operations.
         *
         * @param args Command line arguments passed to the application
         */
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<MetaAgentApplication>(*args)
        }
    }
}