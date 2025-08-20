#!/bin/bash
set -e

echo "Starting analysis for repository: $REPO"
echo "Head SHA: $HEAD_SHA"

# Create a temporary directory for the repo
TEMP_DIR=$(mktemp -d)
echo "Created temporary directory: $TEMP_DIR"
cd "$TEMP_DIR"

# Clone the repository with authentication
echo "Cloning repository..."
if [ -n "$GITHUB_TOKEN" ]; then
    echo "Using GitHub token for authentication"
    git clone https://$GITHUB_TOKEN@github.com/$REPO repo
else
    echo "No GitHub token provided, using public access"
    git clone https://github.com/$REPO repo
fi

cd repo
echo "Fetching all branches and commits..."
git fetch --all

echo "Checking out commit: $HEAD_SHA"
git checkout $HEAD_SHA

# Run analysis
echo "Running Maven analysis..."
mvn -B checkstyle:checkstyle spotbugs:spotbugs -DskipTests -q

# Copy results back to the application directory
echo "Copying results to artifacts directory..."
mkdir -p /app/artifacts
if [ -f target/checkstyle-result.xml ]; then
    cp target/checkstyle-result.xml /app/artifacts/
    echo "Checkstyle results copied"
else
    echo "No checkstyle results found"
fi

if [ -f target/spotbugsXml.xml ]; then
    cp target/spotbugsXml.xml /app/artifacts/
    echo "SpotBugs results copied"
else
    echo "No SpotBugs results found"
fi

# Clean up
echo "Cleaning up temporary directory..."
cd /app
rm -rf "$TEMP_DIR"
echo "Analysis completed successfully"
