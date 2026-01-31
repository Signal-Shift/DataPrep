package com.originspecs.specextractor.reader;

import com.originspecs.specextractor.model.Employee;
import com.originspecs.specextractor.model.Vehicle;
import com.originspecs.specextractor.processor.RowParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Slf4j
public class FileReader <T>{

    private final RowParser rowParser;

    public FileReader() {
        this.rowParser = new RowParser();
    }

    public FileReader(RowParser rowParser) {
        this.rowParser = rowParser;
    }

    public List<T> readXls(String inputPath) throws IOException {
        log.info("Reading XLS file: {}", inputPath);

        try (var fis = new FileInputStream(inputPath);
             var workbook = new HSSFWorkbook(fis)) {

            var sheet = workbook.getSheetAt(0);
            var formatter = new DataFormatter();

            List<T> fileData = extractRecords(sheet, formatter);

            log.info("Read {} records from XLS file", fileData.size());
            return fileData;

        } catch (IOException e) {
            log.error("Error reading XLS file: {}", inputPath, e);
            throw e;
        }
    }

    private List<T> extractRecords(Sheet sheet, DataFormatter formatter) {
        var totalRows = sheet.getLastRowNum();
        log.debug("Extracting records from {} rows (excluding header)", totalRows);

        var records = IntStream.rangeClosed(1, totalRows)
                .mapToObj(rowIndex -> {
                    var row = sheet.getRow(rowIndex);
                    if (row == null) {
                        log.trace("Row {} is null, skipping", rowIndex);
                        return null;
                    }
                    if (row.getLastCellNum() < 7) {
                        log.trace("Row {} has insufficient cells, skipping",
                                rowIndex);
                        return null;
                    }
                    return rowParser.parse(row, formatter).orElse(null);
                })
                .filter(Objects::nonNull)
                .toList();

        log.debug("Successfully parsed {} out of {} rows", employees.size(), totalRows);
        return employees;
    }
}