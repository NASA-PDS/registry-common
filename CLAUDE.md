# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java library providing common functionality for interacting with the PDS (Planetary Data System) Registry and Elasticsearch/OpenSearch. It serves as a shared dependency for registry components like Harvest and Registry Manager.

**Key characteristics:**
- Maven-based Java project (Java 17)
- Version: 2.3.0
- Supports both Elasticsearch REST client (SDK v1) and OpenSearch Java SDK (SDK v2)
- Provides abstraction layer for multiple connection types: direct, Cognito-authenticated, and EC2

## Build Commands

```bash
# Build the project
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run tests
mvn test

# Run a single test
mvn test -Dtest=TestClassName

# Package the JAR
mvn package

# Create source JAR (for debugging)
mvn source:jar

# Clean build artifacts
mvn clean
```

## Code Architecture

### Connection Architecture

The library implements a dual-SDK architecture to support both Elasticsearch REST client and OpenSearch SDK:

**Connection Factory Pattern:**
- `ConnectionFactory` interface - defines contract for creating connections
- `EstablishConnectionFactory` - factory entry point that routes to appropriate SDK implementation
- `UseOpensearchSDK1` - implements connections using Elasticsearch REST client (older, direct connections)
- `UseOpensearchSDK2` - implements connections using OpenSearch Java SDK (supports AWS Cognito, EC2, serverless)

**Connection Types:**
1. **Direct connections** - Direct HTTP/HTTPS to OpenSearch/Elasticsearch (supports both SDK v1 and v2)
2. **Cognito connections** - AWS Cognito authentication for serverless OpenSearch (SDK v2 only)
3. **EC2 connections** - EC2 instance role credentials (SDK v2 only)

Connection configurations are stored as XML files in `src/main/resources/connections/` with separate subdirectories for `cognito/` and `direct/` connection types.

### Core Packages

**`gov.nasa.pds.registry.common.connection`** - Connection management and authentication
- `aws/` - AWS OpenSearch SDK v2 implementations (serverless support)
- `es/` - Elasticsearch REST client SDK v1 implementations
- `config/` - JAXB-generated classes for parsing connection XML configurations

**`gov.nasa.pds.registry.common.meta`** - Metadata extraction from PDS labels
- Extractors for different product types: Bundle, Collection, File, etc.
- `Metadata` class represents extracted metadata with LID, LIDVID, internal references, and fields
- `MetadataNormalizer` handles field normalization for Elasticsearch indexing

**`gov.nasa.pds.registry.common.dd`** - Data Dictionary handling
- Maps PDS data types to Elasticsearch data types
- Parses LDD (Local Data Dictionary) and PDD (Planetary Data Dictionary) files
- `DDRecord` represents data dictionary entries for indexing

**`gov.nasa.pds.registry.common.es`** - Elasticsearch/OpenSearch operations
- `dao/` - Data Access Objects for products, schema, and data dictionaries
- `service/` - Higher-level services like ProductService, CollectionInventoryWriter

**`gov.nasa.pds.registry.common.util`** - Utility classes for file handling, date parsing, XML/JSON processing

### Request/Response Abstraction

The library provides a unified `Request` and `Response` interface that abstracts over both SDKs:
- `RestClient` interface defines operations (bulk, search, count, get, delete, etc.)
- SDK-specific implementations in `connection/aws/` and `connection/es/` wrap native SDK objects
- This allows client code (Harvest, Registry Manager) to work with either SDK transparently

### Known Registries

`KnownRegistryConnections` discovers and lists all available connection configurations by scanning resources. It initializes a custom URL protocol handler (`app://`) to load connection configs from classpath resources.

## Development Notes

**Testing:**
- Tests are in `src/test/java/`
- Test classes organized by functionality: `dao/`, `meta/`, `tt/` (technical tests), `xml/`
- Some tests may require a running Elasticsearch/OpenSearch instance

**Connection configuration:**
- XML schema defines connection types and their properties
- SDK version is specified in XML: `<server_url sdk="1">` or `sdk="2"`
- Default SDK for direct connections is 1 (Elasticsearch REST client)
- Cognito and EC2 connections always use SDK 2

**Pre-commit hooks:**
- Secret detection using `slim-detect-secrets`
- Run `pre-commit install` to enable hooks locally
- Baseline file: `.secrets.baseline`

**CI/CD:**
- Unstable builds (main branch): `.github/workflows/unstable-cicd.yaml`
- Stable builds (releases): `.github/workflows/stable-cicd.yaml`
- Builds publish to Maven Central via Sonatype Central Portal
- Requires secrets: `CENTRAL_REPOSITORY_USERNAME`, `CENTRAL_REPOSITORY_TOKEN`, `CODE_SIGNING_KEY`

## Important Patterns

**Metadata field naming:**
- Uses PDS4 hierarchical naming: `namespace:ClassName/namespace:attribute_name`
- Namespace separator: `:` (defined in `MetaConstants.NS_SEPARATOR`)
- Attribute separator: `/` (defined in `MetaConstants.ATTR_SEPARATOR`)
- Example: `pds:Product_Observational/pds:title`

**Error handling:**
- `ResponseException` wraps SDK-specific exceptions
- SDK1 uses `ResponseExceptionWrapper` to wrap Elasticsearch exceptions
- SDK2 uses AWS SDK exceptions directly

**Resource management:**
- `RestClient` implements `Closeable` - always use try-with-resources
- `CloseUtils` provides helper methods for safely closing resources
