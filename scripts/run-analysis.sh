#!/bin/bash
set -e

# Create a temporary directory for the repo
TEMP_DIR=$(mktemp -d)
cd "$TEMP_DIR"

# Clone the repository
git clone --depth 1 https://github.com/$REPO repo
cd repo
git checkout $HEAD_SHA

# Run analysis (suppress output for cleaner logs)
mvn -B checkstyle:checkstyle spotbugs:spotbugs -DskipTests -q

# Copy results back to the application directory
mkdir -p /app/artifacts
cp target/checkstyle-result.xml /app/artifacts/ 2>/dev/null || echo "No checkstyle results found"
cp target/spotbugsXml.xml /app/artifacts/ 2>/dev/null || echo "No spotbugs results found"

# Clean up
cd /app
rm -rf "$TEMP_DIR"
