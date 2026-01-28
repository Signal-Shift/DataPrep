package com.originspecs.specextractor.reader;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileReader {

    public void readXls(String inputPath, String outputPath) throws FileNotFoundException {


        Path path = Path.of(inputPath);
        if (Files.notExists(path)){
            log.error("File not found: {}", inputPath);
            return;
        }
        log.info("Opening xls file: {} ", inputPath);

       try {
           var fis = new FileInputStream(path.toFile());
           var workbook = new HSSFWorkbook(fis);

           var sheet = workbook.getSheetAt(0);
           var formatter = new DataFormatter();

           for (Row row : sheet) {
               for (Cell cell : row) {
                   String text = formatter.formatCellValue(cell);
                   log.trace("Cell Value: {}", text);
               }

           }
       } catch (IOException e){
           log.error("Error reading file {}, {}", inputPath, e.getMessage());
        }

    }

}
