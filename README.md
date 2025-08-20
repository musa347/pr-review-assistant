# PR Review Assistant

The PR Review Assistant automates the first pass of code review so developers can focus on high-level design instead of repetitive checks. It provides instant, consistent, and actionable feedback to make reviews faster, help PRs merge sooner, and catch bugs earlier.

## 🚀 Features

- **Focused Analysis**: Scans only changed code (diff), not the whole repository
- **Comprehensive Checks**: Runs static analysis for style, bugs, and security issues
- **Smart Testing**: Runs tests selectively to check what the PR breaks
- **AI-Powered Insights**: Uses AI to summarize changes, suggest fixes, and generate comments
- **Seamless Integration**: Posts findings directly into GitHub/GitLab PRs as inline comments and summary reports

## 🏗️ Architecture

The PR Review Assistant is built as a Spring Boot application with the following components:

### 1. Webhook Integration
- Listens for PR events (opened, synchronized, reopened)
- Extracts repository and PR metadata
- Enqueues analysis jobs

### 2. Job Processing
- Asynchronous job queue for handling PR analysis requests
- Scheduled job processor to manage analysis workload

### 3. Analysis Engine
- Clones repositories at specific commits
- Computes diffs to identify changed files and lines
- Runs static analysis tools (Checkstyle, SpotBugs, etc.)
- Executes targeted tests for affected code

### 4. Results Publisher
- Formats analysis findings into actionable comments
- Posts results back to GitHub/GitLab PRs
- Generates summary reports with key metrics

### 5. Security & Sandboxing
- Executes analysis in isolated Docker containers
- Ensures secure handling of repository code

## 🛠️ Technical Implementation

### Core Components

- **GitHub Webhook Controller**: Receives and processes PR events
- **Job Queue**: Manages asynchronous processing of analysis requests
- **Analysis Runner**: Executes static analysis and testing tools
- **Publisher**: Posts results back to the PR

### Technology Stack

- **Backend**: Java 17, Spring Boot 3.x
- **Build System**: Maven
- **Static Analysis**: Checkstyle, SpotBugs
- **Messaging**: Spring Kafka (for scalable job processing)
- **Containerization**: Docker (for secure execution)

## 🚦 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker (for containerized execution)
- GitHub/GitLab account with admin access to repositories

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/pr-review-assistant.git
   cd pr-review-assistant
