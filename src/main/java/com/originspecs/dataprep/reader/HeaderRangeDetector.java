package com.originspecs.dataprep.reader;

import com.originspecs.dataprep.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import java.util.Optional;
import java.util.Set;

/**
 * Detects the full header row range within an XLS sheet.
 *
 * <p>Detection strategy:
 * <ol>
 *   <li>Scan rows to find the one containing {@link Constants#CAR_NAME_JP} ("車名") —
 *       this anchors the header block and is used to find where the block starts.</li>
 *   <li>Walk backwards from that row to find the header block start: the first row
 *       with fewer than {@value MIN_HEADER_CELLS} non-empty cells is treated as
 *       pre-header metadata; the block starts at the next row.</li>
 *   <li>From the "車名" row, scan <em>forward</em> until column A contains a known
 *       Japanese brand name — that row is the data start; the header range ends at
 *       the row immediately before it. This correctly captures sub-header rows that
 *       sit below the "車名" row.</li>
 * </ol>
 *
 * <p>If no brand names are provided, the "車名" row is used as the header range end
 * (legacy fallback behaviour).
 */
@Slf4j
public class HeaderRangeDetector {

    private static final int MIN_HEADER_CELLS = 3;

    private final DataFormatter formatter = new DataFormatter();
    private final Set<String> japaneseBrandNames;

    /** Creates a detector without brand-based data-start detection (legacy fallback). */
    public HeaderRangeDetector() {
        this(Set.of());
    }

    /**
     * Creates a detector that scans forward past "車名" to find where car data actually
     * starts, using the provided set of Japanese brand names.
     *
     * @param japaneseBrandNames Set of Japanese car brand names (e.g. "ホンダ", "トヨタ")
     */
    public HeaderRangeDetector(Set<String> japaneseBrandNames) {
        this.japaneseBrandNames = japaneseBrandNames;
    }

    /**
     * Detects the header range for the given sheet.
     *
     * @param sheet The POI sheet to analyse
     * @return Optional containing the detected HeaderRange, or empty if "車名" is not found
     */
    public Optional<HeaderRange> detect(Sheet sheet) {
        int carNameRowIndex = findCarNameRowIndex(sheet);

        if (carNameRowIndex == -1) {
            log.warn("Could not find '{}' in sheet '{}' — header range detection failed",
                    Constants.CAR_NAME_JP, sheet.getSheetName());
            return Optional.empty();
        }

        int startRowIndex = findHeaderRangeStart(sheet, carNameRowIndex);
        int endRowIndex = findHeaderRangeEnd(sheet, carNameRowIndex);

        HeaderRange range = new HeaderRange(startRowIndex, endRowIndex);

        log.info("Sheet '{}': detected header range rows {}-{}, data starts at row {}",
                sheet.getSheetName(), startRowIndex, endRowIndex, range.dataStartRowIndex());

        return Optional.of(range);
    }

    /**
     * Scans all rows to find the first one containing the "車名" value in any cell.
     *
     * @return 0-based row index, or -1 if not found
     */
    private int findCarNameRowIndex(Sheet sheet) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (Constants.CAR_NAME_JP.equals(formatter.formatCellValue(cell).strip())) {
                    log.debug("Found '{}' at row {} in sheet '{}'",
                            Constants.CAR_NAME_JP, row.getRowNum(), sheet.getSheetName());
                    return row.getRowNum();
                }
            }
        }
        return -1;
    }

    /**
     * Walks backwards from the "車名" row to find where the header block starts.
     * Stops at the first row with fewer than {@value MIN_HEADER_CELLS} non-empty cells.
     */
    private int findHeaderRangeStart(Sheet sheet, int carNameRowIndex) {
        for (int i = carNameRowIndex - 1; i >= 0; i--) {
            Row row = sheet.getRow(i);
            if (row == null || countNonEmptyCells(row) < MIN_HEADER_CELLS) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * Scans forward from the "車名" row to find where real car data begins.
     * The header range ends at the row immediately before the first row whose
     * column A value matches a known Japanese brand name.
     *
     * <p>Falls back to {@code carNameRowIndex} if no brand names are configured
     * or no brand row is found within a reasonable look-ahead window.
     *
     * @param sheet           The POI sheet to scan
     * @param carNameRowIndex The row containing "車名"
     * @return 0-based index of the last header row
     */
    private int findHeaderRangeEnd(Sheet sheet, int carNameRowIndex) {
        if (japaneseBrandNames.isEmpty()) {
            log.debug("No brand names configured — using '車名' row {} as header range end", carNameRowIndex);
            return carNameRowIndex;
        }

        int lastRow = sheet.getLastRowNum();
        for (int i = carNameRowIndex + 1; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell firstCell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (firstCell == null) continue;

            String colAValue = formatter.formatCellValue(firstCell).strip();
            if (japaneseBrandNames.contains(colAValue)) {
                log.debug("Sheet '{}': found brand '{}' at row {} — header range ends at row {}",
                        sheet.getSheetName(), colAValue, i, i - 1);
                return i - 1;
            }
        }

        log.warn("Sheet '{}': no brand name found after '車名' row {} — falling back to '車名' row as header range end",
                sheet.getSheetName(), carNameRowIndex);
        return carNameRowIndex;
    }

    private int countNonEmptyCells(Row row) {
        int count = 0;
        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).strip().isEmpty()) {
                count++;
            }
        }
        return count;
    }
}
