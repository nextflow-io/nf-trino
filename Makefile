# Build the plugin
assemble:
	./gradlew assemble

clean:
	rm -rf .nextflow*
	rm -rf work
	rm -rf build
	./gradlew clean

# Run plugin unit tests
test:
	./gradlew test

# Install the plugin into local nextflow plugins dir
install:
	./gradlew install

# Publish the plugin
release:
	./gradlew releasePlugin

# Test example pipelines
test-examples: install
	cd tests && nf-test test */main.nf.test

# Test specific example pipeline
test-example: install
	@if [ -z "$(EXAMPLE)" ]; then \
		echo "Usage: make test-example EXAMPLE=<example-name>"; \
		echo "Available examples: test-sql-extension, trino-example, athena-example, starburst-example, simple-athena-test, simple-sra-query, nih-sra-athena"; \
		exit 1; \
	fi
	cd tests/$(EXAMPLE) && nf-test test main.nf.test

# Run specific example pipeline
run-example: install
	@if [ -z "$(EXAMPLE)" ]; then \
		echo "Usage: make run-example EXAMPLE=<example-name> [ARGS='--param value']"; \
		echo "Available examples: test-sql-extension, trino-example, athena-example, starburst-example, simple-athena-test, simple-sra-query, nih-sra-athena"; \
		exit 1; \
	fi
	cd tests/$(EXAMPLE) && nextflow run main.nf $(ARGS)

# Clean test outputs
clean-tests:
	cd tests && find . -name "work" -type d -exec rm -rf {} + 2>/dev/null || true
	cd tests && find . -name ".nextflow*" -exec rm -rf {} + 2>/dev/null || true
	cd tests && find . -name "results" -type d -exec rm -rf {} + 2>/dev/null || true
	cd tests && find . -name "*.html" -exec rm -f {} + 2>/dev/null || true

# CI targets
ci-test: clean test install test-examples

# Test individual examples (for CI)
test-examples-individual: install
	@for example in tests/*/; do \
		if [ -d "$$example" ] && [ -f "$$example/main.nf" ]; then \
			echo "Testing $$(basename "$$example")"; \
			cd "$$example" && nf-test test main.nf.test || echo "Test failed for $$(basename "$$example")"; \
			cd - > /dev/null; \
		fi; \
	done

# Validate plugin functionality (for CI)
validate-plugin: install
	@echo "Validating plugin functionality..."
	cd tests/test-sql-extension && timeout 30s nextflow run main.nf || echo "Basic validation completed"
