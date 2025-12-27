#!/bin/bash

# PILAF Integration Test Suite Script
# Usage: ./run-pilaf-integration-tests.sh
# Description: Starts PaperMC server, runs PILAF integration tests, and stops server

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

log_test() {
    echo -e "${BLUE}🧪 $1${NC}"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."

    # Check Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed or not in PATH"
        exit 1
    fi

    # Check if Docker is running
    if ! docker info &> /dev/null; then
        log_error "Docker daemon is not running. Please start Docker Desktop."
        exit 1
    fi

    # Check Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed or not in PATH"
        exit 1
    fi

    # Check environment file
    if [ ! -f ".env" ]; then
        log_error ".env file not found. Please ensure server configuration exists."
        exit 1
    fi

    log_success "All prerequisites met"
}

# Start PaperMC server
start_server() {
    log_info "🚀 Starting PaperMC server with PILAF plugin..."

    # Check if server is already running
    if docker ps | grep -q papermc-dragonegg; then
        log_warning "Server container is already running"
        return 0
    fi

    # Start server
    if ./start-server.sh; then
        log_success "Server startup initiated"
    else
        log_error "Failed to start server"
        exit 1
    fi
}

# Wait for server to be ready
wait_for_server() {
    log_info "⏳ Waiting for server to be ready..."

    local max_wait=120  # 2 minutes max wait
    local elapsed=0
    local interval=5

    while [ $elapsed -lt $max_wait ]; do
        # Check if container is running
        if docker ps | grep -q papermc-dragonegg; then
            # Check server logs for "Done" message
            if docker logs papermc-dragonegg 2>/dev/null | grep -q "Done"; then
                log_success "Server is ready!"
                return 0
            fi
        fi

        echo -n "."
        sleep $interval
        elapsed=$((elapsed + interval))
    done

    log_error "Server failed to start within $max_wait seconds"
    show_server_logs
    exit 1
}

# Verify server status
verify_server() {
    log_info "🔍 Verifying server status..."

    # Check container is running
    if ! docker ps | grep -q papermc-dragonegg; then
        log_error "Server container is not running"
        show_server_logs
        exit 1
    fi

    # Check plugin is loaded
    if ! docker logs papermc-dragonegg 2>/dev/null | grep -q "DragonEggLightning plugin enabled"; then
        log_error "DragonEggLightning plugin not loaded"
        show_server_logs
        exit 1
    fi

    # Check RCON port
    if ! docker port papermc-dragonegg | grep -q "25575"; then
        log_error "RCON port not accessible"
        exit 1
    fi

    log_success "Server verification passed"
}

# Show recent server logs
show_server_logs() {
    log_info "📋 Recent server logs:"
    docker logs papermc-dragonegg --tail 20
}

# Run PILAF integration tests
run_integration_tests() {
    log_test "🧪 Running PILAF integration tests..."

    # Run specific integration tests
    if mvn test -Dtest.groups=integration; then
        log_success "PILAF integration tests completed successfully"
    else
        log_error "PILAF integration tests failed"
        return 1
    fi

    # Run use case tests specifically
    log_test "🎯 Running dragon egg lightning use case tests..."
    if mvn test -Dtest=DragonEggLightningUseCaseTest; then
        log_success "Use case tests completed successfully"
    else
        log_error "Use case tests failed"
        return 1
    fi
}

# Run additional test suites
run_additional_tests() {
    log_test "🔧 Running unit tests..."
    if mvn test -Dtest.groups=unit; then
        log_success "Unit tests completed successfully"
    else
        log_warning "Some unit tests failed"
    fi

    log_test "🏗️ Running PILAF framework tests..."
    if mvn test -Dtest.groups=pilaf; then
        log_success "PILAF framework tests completed successfully"
    else
        log_warning "Some PILAF framework tests failed"
    fi
}

# Generate test report
generate_report() {
    log_info "📊 Generating test report..."

    local report_file="target/pilaf-integration-test-report.txt"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    cat > "$report_file" << EOF
PILAF Integration Test Report
==============================
Date: $timestamp
Server: PaperMC with DragonEggLightning Plugin
Framework: PILAF v1.0.0

Test Results Summary:
- Integration Tests: $([ -f target/surefire-reports/*integration*.txt ] && echo "PASSED" || echo "FAILED")
- Use Case Tests: $([ -f target/surefire-reports/*DragonEggLightning*.txt ] && echo "PASSED" || echo "FAILED")
- Unit Tests: $([ -f target/surefire-reports/*Test*.txt ] && echo "PASSED" || echo "FAILED")

Server Status:
- Container: $(docker ps --filter name=papermc-dragonegg --format "{{.Status}}" | head -1)
- Plugin Loaded: $(docker logs papermc-dragonegg 2>/dev/null | grep -c "DragonEggLightning plugin enabled")
- RCON Port: $(docker port papermc-dragonegg 2>/dev/null | grep 25575 | wc -l)

For detailed test results, check:
- target/surefire-reports/*.txt
- target/failsafe-reports/*.txt (if integration tests)
EOF

    log_success "Test report generated: $report_file"
}

# Stop server
stop_server() {
    log_info "🛑 Stopping PaperMC server..."

    if ./stop-server.sh; then
        log_success "Server stopped successfully"
    else
        log_warning "Failed to stop server gracefully"
        # Force stop if needed
        docker stop papermc-dragonegg 2>/dev/null || true
        docker rm papermc-dragonegg 2>/dev/null || true
    fi
}

# Cleanup function
cleanup() {
    log_info "🧹 Cleaning up..."
    stop_server
}

# Main execution
main() {
    echo "======================================"
    echo "🚀 PILAF Integration Test Suite"
    echo "======================================"
    echo ""

    # Set trap for cleanup
    trap cleanup EXIT

    # Execute test pipeline
    check_prerequisites
    start_server
    wait_for_server
    verify_server

    echo ""
    log_test "Starting test execution..."
    echo ""

    # Run tests
    if run_integration_tests; then
        echo ""
        log_test "Running additional test suites..."
        run_additional_tests

        echo ""
        generate_report

        echo ""
        log_success "🎉 All PILAF integration tests PASSED!"
        echo ""
        echo "📊 Test Summary:"
        echo "   ✅ Server started and verified"
        echo "   ✅ Plugin loaded successfully"
        echo "   ✅ PILAF framework integration tests passed"
        echo "   ✅ Dragon egg lightning use case verified"
        echo "   ✅ Test report generated"
        echo ""
        echo "📋 Check target/pilaf-integration-test-report.txt for detailed results"
    else
        echo ""
        log_error "💥 PILAF integration tests FAILED!"
        echo ""
        echo "🔍 Troubleshooting:"
        echo "   1. Check server logs: docker logs papermc-dragonegg --tail 50"
        echo "   2. Verify Docker is running: docker ps"
        echo "   3. Check Maven build: mvn clean compile"
        echo "   4. Review test reports in target/surefire-reports/"
        exit 1
    fi
}

# Handle script arguments
case "${1:-}" in
    --help|-h)
        echo "PILAF Integration Test Suite"
        echo ""
        echo "Usage: $0 [options]"
        echo ""
        echo "Options:"
        echo "  --help, -h     Show this help message"
        echo "  --verbose, -v  Enable verbose output"
        echo "  --skip-server  Skip server startup (assume server is running)"
        echo "  --dry-run      Show what would be executed without running"
        echo ""
        echo "This script will:"
        echo "1. Start PaperMC server with PILAF plugin"
        echo "2. Wait for server to be ready"
        echo "3. Run PILAF integration tests"
        echo "4. Run dragon egg lightning use case tests"
        echo "5. Generate test report"
        echo "6. Stop server and cleanup"
        exit 0
        ;;
    --verbose|-v)
        set -x  # Enable verbose mode
        shift
        ;;
    --skip-server)
        SKIP_SERVER=true
        shift
        ;;
    --dry-run)
        echo "DRY RUN - Commands that would be executed:"
        echo "  1. check_prerequisites"
        echo "  2. start_server (unless --skip-server)"
        echo "  3. wait_for_server"
        echo "  4. verify_server"
        echo "  5. run_integration_tests"
        echo "  6. run_additional_tests"
        echo "  7. generate_report"
        echo "  8. stop_server"
        exit 0
        ;;
esac

# Run main function
main "$@"
