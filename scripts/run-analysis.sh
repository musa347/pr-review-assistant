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

# Get the list of changed files (Java files only)
echo "Getting list of changed files..."
echo "Base SHA: $BASE_SHA"
CHANGED_FILES=$(git diff --name-only $BASE_SHA HEAD | grep '\.java$' || true)

if [ -z "$CHANGED_FILES" ]; then
    echo "No Java files changed in this PR"
    # Create empty result files
    mkdir -p target
    echo '<?xml version="1.0" encoding="UTF-8"?><checkstyle version="8.45.1"><file name="no-files-changed"></file></checkstyle>' > target/checkstyle-result.xml
    echo '<?xml version="1.0" encoding="UTF-8"?><BugCollection version="4.7.3" sequence="0" timestamp="0" analysisTimestamp="0" release=""></BugCollection>' > target/spotbugsXml.xml
else
    echo "Changed Java files:"
    echo "$CHANGED_FILES"
    
    # Run analysis only on changed files
    echo "Running Maven analysis on changed files..."
    
    # First compile the project (needed for SpotBugs)
    mvn -B compile -DskipTests -q
    
    # For now, let's just run checkstyle on all files to ensure it works
    # We'll implement filtering in the Java parser instead
    echo "Running Checkstyle on all files..."
    mvn -B checkstyle:checkstyle -DskipTests -q
    
    echo "Checkstyle completed. Changed files for reference: $CHANGED_FILES"
    
    # Save the list of changed files for the Java parser to use
    echo "$CHANGED_FILES" > /app/artifacts/changed-files.txt
    
    # Run SpotBugs (it will analyze only compiled classes, so effectively only changed files)
    echo "Running SpotBugs..."
    mvn -B spotbugs:spotbugs -DskipTests -q
fi

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
