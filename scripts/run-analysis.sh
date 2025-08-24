#!/bin/bash
set -e

APP_DIR="$(pwd -P)"  # Remember app dir to copy artifacts back

echo "Starting analysis for repository: $REPO"
echo "Head SHA: $HEAD_SHA"
echo "App directory: $APP_DIR"

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

# Prefer the project's Maven Wrapper if available
MVN_CMD="./mvnw"
if [ -f "mvnw" ]; then
  chmod +x mvnw || true
else
  MVN_CMD="mvn"
fi

detect_java_version() {
  local ver
  # Try maven.compiler.release first
  ver=$($MVN_CMD -q -Dexpression=maven.compiler.release help:evaluate -DforceStdout 2>/dev/null || true)
  if [[ -z "$ver" || "$ver" == "\${maven.compiler.release}" ]]; then
    ver=$($MVN_CMD -q -Dexpression=maven.compiler.target help:evaluate -DforceStdout 2>/dev/null || true)
  fi
  if [[ -z "$ver" || "$ver" == "\${maven.compiler.target}" ]]; then
    ver=$($MVN_CMD -q -Dexpression=maven.compiler.source help:evaluate -DforceStdout 2>/dev/null || true)
  fi
  echo "$ver"
}

DEFAULT_JAVA_HOME="${JAVA_HOME}"
REQUIRED_JAVA="$(detect_java_version)"
echo "Detected required Java version: ${REQUIRED_JAVA:-unknown}"

# Switch JAVA_HOME to match REQUIRED_JAVA
CANDIDATE_JAVA_HOME=""
if [[ -n "$REQUIRED_JAVA" ]]; then
  # macOS: try /usr/libexec/java_home
  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    case "$REQUIRED_JAVA" in
      17|21|22|23)
        CANDIDATE_JAVA_HOME="$(/usr/libexec/java_home -v "$REQUIRED_JAVA" 2>/dev/null || true)"
        ;;
    esac
  fi
  # Linux container fallback: /opt/jdk-<version>
  if [[ -z "$CANDIDATE_JAVA_HOME" && -d "/opt/jdk-$REQUIRED_JAVA" ]]; then
    CANDIDATE_JAVA_HOME="/opt/jdk-$REQUIRED_JAVA"
  fi
  # If project needs 23+ and exact version not present, use JDK 23 if available
  if [[ -z "$CANDIDATE_JAVA_HOME" && "$REQUIRED_JAVA" =~ ^2[3-9]$ && -d "/opt/jdk-23" ]]; then
    CANDIDATE_JAVA_HOME="/opt/jdk-23"
  fi
fi

if [[ -n "$CANDIDATE_JAVA_HOME" ]]; then
  export JAVA_HOME="$CANDIDATE_JAVA_HOME"
  export PATH="$JAVA_HOME/bin:$PATH"
  echo "Switched JAVA_HOME to JDK $REQUIRED_JAVA at $JAVA_HOME"
else
  export JAVA_HOME="$DEFAULT_JAVA_HOME"
  echo "Using default JAVA_HOME=$JAVA_HOME"
fi

echo "java -version:"
java -version

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
    
    # Compile the project (needed for SpotBugs)
    $MVN_CMD -B compile -DskipTests -q
    
    # Run Checkstyle with an explicit plugin version supporting newer JDKs
    echo "Running Checkstyle..."
    $MVN_CMD -B org.apache.maven.plugins:maven-checkstyle-plugin:3.6.0:checkstyle -DskipTests -q
    
    echo "Checkstyle completed. Changed files for reference: $CHANGED_FILES"
    
    # Save the list of changed files for the Java parser to use
    mkdir -p "$APP_DIR/artifacts"
    echo "$CHANGED_FILES" > "$APP_DIR/artifacts/changed-files.txt"
    
    # Run SpotBugs with a recent plugin version
    echo "Running SpotBugs..."
    $MVN_CMD -B com.github.spotbugs:spotbugs-maven-plugin:4.8.5.0:spotbugs -DskipTests -q
fi

# Copy results back to the application directory
echo "Copying results to artifacts directory..."
mkdir -p "$APP_DIR/artifacts"
if [ -f target/checkstyle-result.xml ]; then
    cp target/checkstyle-result.xml "$APP_DIR/artifacts/"
    echo "Checkstyle results copied"
else
    echo "No checkstyle results found"
fi

if [ -f target/spotbugsXml.xml ]; then
    cp target/spotbugsXml.xml "$APP_DIR/artifacts/"
    echo "SpotBugs results copied"
else
    echo "No SpotBugs results found"
fi

# Clean up
echo "Cleaning up temporary directory..."
cd "$APP_DIR"
rm -rf "$TEMP_DIR"
echo "Analysis completed successfully"
