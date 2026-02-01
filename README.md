# DataPrep

A lightweight Java CLI tool that performs pre-processing on XLS files to match the desried output structure.
Normalizing messy data/columns mismatch in order to create structured JSON after this data is fed to the [spec-extractor](https://github.com/DylanBrennan92/spec-extractor)
application. Output of DataPrep is normalised XLS files.
## Technology Stack

- Java 21 - Modern Java with records, sealed interfaces, and pattern matching
- Apache POI 5.4.0 - Excel file parsing and data extraction
- Jackson 2.17.0 - JSON serialization with pretty printing
- Lombok - Reduced boilerplate code
- SLF4J & Logback - Configurable logging with multiple log levels
- Maven - Build automation and dependency management

### Dependencies

* Java 21
* Maven

### Building from source

git clone https://github.com/DylanBrennan92/DataPrep
cd DataPrep
mvn clean package

### CLI Arguments

| Argument | Required | Description | Example |
|----------|----------|-------------|---------|
| `recordType` | Yes | Record type to extract: `VEHICLE` | `VEHICLE` |
| `inputFile.xls` | Yes | Path to the Excel (.xls) file to process | `nissan.xls`, `staff.xls` |
| `outputFile.json` | Yes | Path where the JSON output should be saved | `output-nissan.json`|
| `requiredCells` | Yes | Minimum number of filled cells for a row to be kept | `20`, `7` |
| `columnThreshold` | No | Minimum fill ratio (0.0–1.0) to keep a column; default `0.1` (10%) | `0.01`, `0.05` |

**Usage:**  
`java -jar DataPrep.jar <recordType> <inputFile.xls> <outputFile.json> <requiredCells> [columnThreshold]`

## Example usage

```bash
# Vehicle specs (e.g. Nissan spreadsheet) — 20 required cells, 1% column threshold
java -jar target/DataPrep-1.0-SNAPSHOT-jar-with-dependencies.jar VEHICLE src/main/resources/nissan.xls output-nissan.json 20 0.01

# With DEBUG logging
java -DLOG_LEVEL=DEBUG -jar target/DataPrep VEHICLE src/main/resources/nissan.xls output-nissan.json 20 0.01
```

## Logging options

Set the `LOG_LEVEL` system property to control verbosity:

```bash
# INFO (default — progress and results)
java -jar target/DataPrep-*.jar VEHICLE input.xls output.json 20

# DEBUG (detailed processing)
java -DLOG_LEVEL=DEBUG -jar target/DataPrep*.jar VEHICLE input.xls output.json 20 0.01

# TRACE (maximum verbosity for troubleshooting)
java -DLOG_LEVEL=TRACE -jar target/DataPrep*.jar VEHICLE input.xls output.json 20 0.01
```