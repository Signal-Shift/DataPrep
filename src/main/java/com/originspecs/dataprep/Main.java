package com.originspecs.dataprep;

import com.originspecs.dataprep.config.Config;
import com.originspecs.dataprep.config.CliParser;
import com.originspecs.dataprep.reader.RowMapper;
import com.originspecs.dataprep.reader.XlsFileReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Main {

    public static void main(String[] args) {

        Config config = CliParser.parseOrExit(args);

        try {

            // Map each POI Row to a List<String> of cell values (generic row type)
            RowMapper<List<String>> rowMapper = row -> {
                int lastCellNum = row.getLastCellNum();
                List<String> cells = new ArrayList<>(lastCellNum);
                for (int i = 0; i < lastCellNum; i++) {
                    Cell cell = row.getCell(i);
                    cells.add(cell == null ? "" : cell.toString());
                }
                return cells;
            };

            XlsFileReader<List<String>> fileReader = new XlsFileReader<>();
            List<List<String>> rows = fileReader.readXlsFileIntoList(config.inputFile(), rowMapper);

            // #TEMP logging to debug

            int rowIndex = 0;
            for (List<String> row : rows) {
                log.info("Row {}: {}", rowIndex++, row);
            }

            // TODO: process rows (e.g. column threshold), then write modified xls via XlsWriter


        } catch (Exception e) {
            log.error(String.valueOf(e));
        }
    }
}