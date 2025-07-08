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
# Run specific example pipeline
run-example:
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
