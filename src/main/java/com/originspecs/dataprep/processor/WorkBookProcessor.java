package com.originspecs.dataprep.processor;

import com.originspecs.dataprep.config.Constants;
import com.originspecs.dataprep.model.RowData;
import com.originspecs.dataprep.model.WorkBookData;
import com.originspecs.dataprep.model.WorkSheetData;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class WorkBookProcessor {

    private final HeaderResolver headerResolver;

    /**
     * Creates a processor with the given permitted headers map.
     * Use {@link com.originspecs.dataprep.config.PermittedHeadersBuilder} to load the map.
     */
    public WorkBookProcessor(Map<String, String> permittedHeaders) {
        this.headerResolver = new HeaderResolver(permittedHeaders);
    }

    /**
     * Creates a processor with an injected {@link HeaderResolver} (for testing).
     */
    public WorkBookProcessor(HeaderResolver headerResolver) {
        this.headerResolver = headerResolver;
    }

    /**
     * Processes a WorkBookData by:
     * <ol>
     *   <li>Removing columns whose data fill ratio is below {@code columnThreshold}</li>
     *   <li>Resolving the multi-row header range into a single English header label per remaining column</li>
     * </ol>
     * Returns a new WorkBookData — the input is never mutated.
     *
     * @param workBook        The workbook to process
     * @param columnThreshold Minimum fill ratio (0.0–1.0) required to keep a column
     * @return New WorkBookData with sparse columns removed and headers resolved
     */
    public WorkBookData process(WorkBookData workBook, double columnThreshold) {
        log.info("Processing workbook '{}' with column threshold: {}",
                workBook.getFileName(), columnThreshold);

        WorkBookData processed = new WorkBookData();
        processed.setFileName(workBook.getFileName());
        processed.setWorksheetCount(workBook.getWorksheetCount());

        for (WorkSheetData sheet : workBook.getWorksheets()) {
            processed.getWorksheets().add(applyColumnThreshold(sheet, columnThreshold));
        }

        return processed;
    }

    private WorkSheetData applyColumnThreshold(WorkSheetData sheet, double threshold) {
        // Step 1: locate the Car Name column — it is always kept regardless of fill ratio
        int carNameColIndex = findCarNameColumnIndex(sheet);

        // Step 2: determine which columns have enough data to keep
        List<Integer> columnsToKeep = determineColumnsToKeep(sheet, threshold, carNameColIndex);

        // Step 3: resolve multi-row headers into a single label per remaining column
        List<String> resolvedHeaders = headerResolver.resolve(sheet.getRawHeaderRows(), columnsToKeep);

        // Step 4: drop columns whose resolved header is empty (e.g. spacer/footnote columns)
        List<Integer> namedColumns = new ArrayList<>();
        List<String> namedHeaders = new ArrayList<>();
        for (int i = 0; i < columnsToKeep.size(); i++) {
            if (resolvedHeaders.get(i).isEmpty()) {
                log.info("Removing unlabelled column {} from '{}' (no header resolved)",
                        columnsToKeep.get(i), sheet.getName());
            } else {
                namedColumns.add(columnsToKeep.get(i));
                namedHeaders.add(resolvedHeaders.get(i));
            }
        }

        // Step 5: resolve duplicates — for columns sharing the same label, keep the
        //         highest-fill column; if fills are similar, keep all with (2)/(3) suffix
        List<Integer> finalColumns = new ArrayList<>();
        List<String> finalHeaders = new ArrayList<>();
        resolveDuplicates(namedHeaders, namedColumns, sheet.getRows(), sheet.getName(),
                finalHeaders, finalColumns);

        WorkSheetData processed = new WorkSheetData();
        processed.setName(sheet.getName());
        processed.setIndex(sheet.getIndex());
        processed.setOriginalRowCount(sheet.getOriginalRowCount());
        processed.setOriginalColumnCount(sheet.getOriginalColumnCount());
        processed.setHeaders(finalHeaders);
        processed.setRows(filterRows(sheet.getRows(), finalColumns));

        log.info("Sheet '{}': {} columns → headers: {}",
                sheet.getName(), finalHeaders.size(), finalHeaders);
        return processed;
    }

    /**
     * Minimum fill-rate ratio below which a duplicate-label column is considered a
     * low-value artefact (e.g. a ※ footnote marker column sitting under a merged
     * group header) and dropped in favour of the higher-fill sibling.
     *
     * <p>Example: if the best-fill "Common Name" column has 20% fill, any other
     * "Common Name" column with fill below {@code 20% × 0.5 = 10%} is dropped.
     * Columns with similar fill rates are kept and numbered (2), (3)…
     */
    private static final double DEDUP_FILL_RATIO_THRESHOLD = 0.5;

    /**
     * Resolves duplicate header labels by comparing data fill rates.
     *
     * <p>For each group of columns that share the same resolved label:
     * <ol>
     *   <li>Find the highest fill rate in the group.</li>
     *   <li>Drop any column whose fill is below
     *       {@value #DEDUP_FILL_RATIO_THRESHOLD} × that maximum — these are
     *       typically footnote/spacer columns that inherited their label from a
     *       merged group header.</li>
     *   <li>If multiple columns survive (genuinely distinct data), append
     *       " (2)", " (3)"… to make each label unique.</li>
     * </ol>
     *
     * @param headers     resolved header labels (parallel to colIndices)
     * @param colIndices  original column indices (before any row filtering)
     * @param rows        raw data rows used to compute fill rates
     * @param sheetName   used in log messages
     * @param outHeaders  populated with the final de-duplicated labels
     * @param outCols     populated with the surviving column indices
     */
    private void resolveDuplicates(List<String> headers, List<Integer> colIndices,
                                   List<RowData> rows, String sheetName,
                                   List<String> outHeaders, List<Integer> outCols) {
        int totalRows = rows.size();

        // Group positions by label (preserving insertion order)
        Map<String, List<Integer>> byLabel = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            byLabel.computeIfAbsent(headers.get(i), k -> new ArrayList<>()).add(i);
        }

        // Decide which positions to drop based on fill rate
        Set<Integer> toDrop = new HashSet<>();
        for (Map.Entry<String, List<Integer>> entry : byLabel.entrySet()) {
            List<Integer> positions = entry.getValue();
            if (positions.size() <= 1) continue;

            double maxFill = positions.stream()
                    .mapToDouble(i -> fillRate(rows, colIndices.get(i), totalRows))
                    .max().orElse(0);

            for (int pos : positions) {
                double fill = fillRate(rows, colIndices.get(pos), totalRows);
                if (maxFill > 0 && fill < maxFill * DEDUP_FILL_RATIO_THRESHOLD) {
                    toDrop.add(pos);
                    log.info("Sheet '{}': dropping duplicate column {} ('{}') — {}% fill vs {}% best fill",
                            sheetName, colIndices.get(pos), entry.getKey(),
                            String.format("%.1f", fill * 100), String.format("%.1f", maxFill * 100));
                }
            }
        }

        // Build output in original order, numbering any remaining duplicates
        Map<String, Integer> seen = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            if (toDrop.contains(i)) continue;

            String label = headers.get(i);
            int count = seen.merge(label, 1, Integer::sum);
            String finalLabel = count == 1 ? label : label + " (" + count + ")";

            if (count > 1) {
                log.info("Sheet '{}': duplicate header '{}' renamed to '{}'", sheetName, label, finalLabel);
            }
            outHeaders.add(finalLabel);
            outCols.add(colIndices.get(i));
        }
    }

    private double fillRate(List<RowData> rows, int colIndex, int totalRows) {
        return totalRows == 0 ? 0 : (double) countNonEmptyCells(rows, colIndex) / totalRows;
    }

    private List<Integer> determineColumnsToKeep(WorkSheetData sheet, double threshold, int protectedColIndex) {
        int totalRows = sheet.getRows().size();
        if (totalRows == 0) {
            log.warn("Sheet '{}' has no data rows, skipping column analysis", sheet.getName());
            return new ArrayList<>();
        }

        List<Integer> columnsToKeep = new ArrayList<>();
        for (int colIndex = 0; colIndex < sheet.getOriginalColumnCount(); colIndex++) {
            double fillPercentage = (double) countNonEmptyCells(sheet.getRows(), colIndex) / totalRows;

            if (colIndex == protectedColIndex) {
                columnsToKeep.add(colIndex);
                log.info("Keeping column {} ('{}') in '{}': {}% fill — protected (Car Name column)",
                        colIndex, Constants.CAR_NAME_JP, sheet.getName(),
                        String.format("%.1f", fillPercentage * 100));
            } else if (fillPercentage >= threshold) {
                columnsToKeep.add(colIndex);
                log.debug("Keeping column {} in '{}': {}% fill",
                        colIndex, sheet.getName(), String.format("%.1f", fillPercentage * 100));
            } else {
                log.info("Removing column {} from '{}': {}% fill (below {}% threshold)",
                        colIndex, sheet.getName(),
                        String.format("%.1f", fillPercentage * 100),
                        String.format("%.1f", threshold * 100));
            }
        }
        return columnsToKeep;
    }

    /**
     * Finds the column index whose raw header rows contain {@link Constants#CAR_NAME_JP}.
     * This column is protected from threshold-based removal.
     *
     * @return The 0-based column index of the Car Name column, or -1 if not found
     */
    private int findCarNameColumnIndex(WorkSheetData sheet) {
        for (List<String> headerRow : sheet.getRawHeaderRows()) {
            for (int colIndex = 0; colIndex < headerRow.size(); colIndex++) {
                if (Constants.CAR_NAME_JP.equals(headerRow.get(colIndex).trim())) {
                    log.debug("Sheet '{}': Car Name column ('{}') found at index {}",
                            sheet.getName(), Constants.CAR_NAME_JP, colIndex);
                    return colIndex;
                }
            }
        }
        log.warn("Sheet '{}': Car Name column ('{}') not found — no column will be protected from threshold",
                sheet.getName(), Constants.CAR_NAME_JP);
        return -1;
    }

    private int countNonEmptyCells(List<RowData> rows, int columnIndex) {
        int count = 0;
        for (RowData row : rows) {
            if (!row.getCell(columnIndex).trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private List<String> filterByIndices(List<String> values, List<Integer> indicesToKeep) {
        List<String> filtered = new ArrayList<>(indicesToKeep.size());
        for (Integer index : indicesToKeep) {
            filtered.add(index < values.size() ? values.get(index) : "");
        }
        return filtered;
    }

    private List<RowData> filterRows(List<RowData> rows, List<Integer> columnsToKeep) {
        List<RowData> filtered = new ArrayList<>(rows.size());
        for (RowData row : rows) {
            filtered.add(new RowData(filterByIndices(row.getCellValues(), columnsToKeep)));
        }
        return filtered;
    }
}
