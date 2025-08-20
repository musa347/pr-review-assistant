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
    
    # Run checkstyle on all files (we'll filter results later)
    echo "Running Checkstyle..."
    mvn -B checkstyle:checkstyle -DskipTests -q
    
    # Filter checkstyle results to only include changed files
    if [ -f target/checkstyle-result.xml ]; then
        echo "Filtering checkstyle results for changed files only..."
        # Create a backup of the original results
        cp target/checkstyle-result.xml target/checkstyle-result-full.xml
        
        # Create filtered results
        python3 -c "
import xml.etree.ElementTree as ET
import sys
import os

# Read the changed files
changed_files = '''$CHANGED_FILES'''.strip().split('\n')
changed_files = [f.strip() for f in changed_files if f.strip()]

if not changed_files:
    sys.exit(0)

# Parse the checkstyle XML
tree = ET.parse('target/checkstyle-result.xml')
root = tree.getroot()

# Filter file elements to only include changed files
files_to_keep = []
for file_elem in root.findall('file'):
    file_name = file_elem.get('name', '')
    # Check if this file is in our changed files list
    for changed_file in changed_files:
        if file_name.endswith(changed_file) or changed_file in file_name:
            files_to_keep.append(file_elem)
            break

# Remove all file elements and add back only the ones we want to keep
for file_elem in root.findall('file'):
    root.remove(file_elem)

for file_elem in files_to_keep:
    root.append(file_elem)

# Write the filtered results
tree.write('target/checkstyle-result.xml', encoding='utf-8', xml_declaration=True)
print(f'Filtered checkstyle results: kept {len(files_to_keep)} files out of changed files')
"
    fi
    
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
