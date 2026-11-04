# Test SQL Extension Pipeline

This simple example tests that the nf-trino plugin SQL extension loads properly without requiring database connections.

## Description

This pipeline shows how to:
- Test that the nf-trino plugin loads correctly
- Verify the SQL extension is available
- Run basic plugin functionality tests
- Use this as a starting point for debugging plugin issues

## Requirements

- nf-trino plugin installed
- No database connections required

## Configuration

The `nextflow.config` file contains minimal configuration:

```groovy
plugins {
    id 'nf-trino'
}
```

## Usage

```bash
# Run the basic test
nextflow run main.nf

# Run with specific plugin version
nextflow run main.nf -plugins nf-trino@0.1.0

# Run in verbose mode to see more details
nextflow run main.nf -v
```

## Parameters

No parameters are required for this pipeline.

## Testing

Run the nf-test suite:

```bash
nf-test test main.nf.test
```

## Expected Output

The pipeline will print success messages if the extension loads properly:

```
Testing nf-trino plugin SQL extension...
✅ SQL extension (fromQuery) is available
🎉 SQL extension test completed!
```

## Troubleshooting

If the test fails, check:

1. **Plugin Installation**: Ensure the nf-trino plugin is properly installed
   ```bash
   nextflow plugin list
   ```

2. **Plugin Version**: Verify you're using a compatible version
   ```bash
   nextflow plugin install nf-trino@latest
   ```

3. **Configuration**: Check that your `nextflow.config` includes the plugin
   ```groovy
   plugins {
       id 'nf-trino'
   }
   ```

4. **Dependencies**: Ensure all required dependencies are available

## Common Error Messages

### Plugin Not Found
```
Unknown plugin id: nf-trino
```
**Solution**: Install the plugin with `nextflow plugin install nf-trino`

### Extension Not Available
```
❌ SQL extension test failed: No signature of method: ...
```
**Solution**: Check plugin version compatibility and installation

### Import Error
```
Script compilation error: unable to resolve class fromQuery
```
**Solution**: Verify the plugin is loaded and the import statement is correct

## Use Cases

This test pipeline is useful for:
- Verifying plugin installation in CI/CD environments
- Debugging plugin loading issues
- Testing plugin compatibility with different Nextflow versions
- Creating minimal examples for issue reporting

## Related Documentation

- [nf-trino Plugin Documentation](../../README.md)
- [Nextflow Plugin Development Guide](https://www.nextflow.io/docs/latest/plugins.html)
- [Plugin Installation Guide](https://www.nextflow.io/docs/latest/plugins.html#plugin-installation)