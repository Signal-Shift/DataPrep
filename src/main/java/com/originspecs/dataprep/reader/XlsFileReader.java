package com.originspecs.dataprep.reader;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

@Slf4j
public class XlsFileReader<T> {

    /**
     * Reads an .xls file and maps each row into a List<T>.
     *
     * @param inputPath  Path to the .xls file
     * @param rowMapper  Maps a Row into a domain object
     * @return List of mapped objects
     */

    public List<T> readXlsFileIntoList(Path inputPath, RowMapper<T> rowMapper) throws IOException {
        log.info("Rading XLS file from {}", inputPath.toAbsolutePath());

        try (InputStream is = Files.newInputStream(inputPath);
             Workbook workbook = new HSSFWorkbook(is)) {

            var sheet = workbook.getSheetAt(0);
            var results = readSheet(sheet, rowMapper);

            log.info("Successfully read {} records from XLS sheet", results.size());

            return results;
        }

    }

    private List<T> readSheet(Sheet sheet, RowMapper<T> rowMapper) {

        return StreamSupport.stream(sheet.spliterator(), false)
                .map(rowMapper::map)
                .filter(Objects::nonNull)
                .toList();
    }



}