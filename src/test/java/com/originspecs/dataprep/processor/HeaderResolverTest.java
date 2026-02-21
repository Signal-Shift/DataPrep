package com.originspecs.dataprep.processor;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HeaderResolver}.
 *
 * Tests cover the three-stage resolution strategy:
 * <ol>
 *   <li>Bottom-to-top scan of multi-row headers</li>
 *   <li>Permitted-header lookup with newline normalisation</li>
 *   <li>Fallback to the bottom-most non-empty value when no match is found</li>
 * </ol>
 */
class HeaderResolverTest {

    private static final Map<String, String> HEADERS = Map.of(
            "車名",   "Car Name",
            "通称名", "Common Name",
            "型式",   "Model Type",
            "総排気量（L）", "Displacement (L)"
    );

    // --- Single header row ---

    @Test
    void resolve_singleHeaderRow_matchedPermittedLabel_returnsTranslation() {
        HeaderResolver resolver = new HeaderResolver(HEADERS);

        List<String> resolved = resolver.resolve(
                List.of(List.of("車名", "通称名", "型式")),
                List.of(0, 1, 2)
        );

        assertThat(resolved).containsExactly("Car Name", "Common Name", "Model Type");
    }

    @Test
    void resolve_singleHeaderRow_noPermittedMatch_returnsCellValueAsIs() {
        HeaderResolver resolver = new HeaderResolver(HEADERS);

        List<String> resolved = resolver.resolve(
                List.of(List.of("未知ラベル")),
                List.of(0)
        );

        assertThat(resolved).containsExactly("未知ラベル");
    }

    // --- Multi-row scanning — bottom-to-top ---

    @Test
    void resolve_multipleHeaderRows_prefersBottomRowMatch() {
        // Bottom row has the specific "型式" label; top row has a generic group heading
        List<List<String>> headerRows = List.of(
                List.of("車両区分", "車両区分", "型式"),   // row 0 (top / general)
                List.of("",         "車名",   "型式")    // row 1 (bottom / specific)
        );
        HeaderResolver resolver = new HeaderResolver(HEADERS);

        List<String> resolved = resolver.resolve(headerRows, List.of(0, 1, 2));

        // Col 0: no value in bottom row, falls through to top row "車両区分" (no match → fallback)
        assertThat(resolved.get(0)).isEqualTo("車両区分");
        // Col 1: bottom row has "車名" which is permitted
        assertThat(resolved.get(1)).isEqualTo("Car Name");
        // Col 2: bottom row has "型式" which is permitted
        assertThat(resolved.get(2)).isEqualTo("Model Type");
    }

    @Test
    void resolve_multipleHeaderRows_onlyTopRowHasMatch_returnsTranslation() {
        // Col 0 label exists only in the top header row
        List<List<String>> headerRows = List.of(
                List.of("車名"),  // top row — permitted match
                List.of("")      // bottom row — empty
        );
        HeaderResolver resolver = new HeaderResolver(HEADERS);

        List<String> resolved = resolver.resolve(headerRows, List.of(0));

        assertThat(resolved).containsExactly("Car Name");
    }

    // --- Newline normalisation ---

    @Test
    void resolve_cellValueWithEmbeddedNewlines_matchedAfterNormalisation() {
        // XLS cells often store multi-line labels; the key in the permitted map is clean
        List<List<String>> headerRows = List.of(
                List.of("総排\n気量\n（L）")  // raw multi-line cell value from XLS
        );
        HeaderResolver resolver = new HeaderResolver(HEADERS);

        List<String> resolved = resolver.resolve(headerRows, List.of(0));

        assertThat(resolved).containsExactly("Displacement (L)");
    }

    @Test
    void resolve_cellValueWithWindowsNewlines_normalisedCorrectly() {
        List<List<String>> headerRows = List.of(
                List.of("総排\r\n気量\r\n（L）")
        );
        HeaderResolver resolver = new HeaderResolver(HEADERS);

        List<String> resolved = resolver.resolve(headerRows, List.of(0));

        assertThat(resolved).containsExactly("Displacement (L)");
    }

    // --- Empty columns ---

    @Test
    void resolve_allHeaderCellsEmptyForColumn_returnsEmptyString() {
        List<List<String>> headerRows = List.of(
                List.of("車名", ""),
                List.of("車名", "")
        );
        HeaderResolver resolver = new HeaderResolver(HEADERS);

        List<String> resolved = resolver.resolve(headerRows, List.of(0, 1));

        assertThat(resolved.get(0)).isEqualTo("Car Name");
        assertThat(resolved.get(1)).isEmpty();
    }

    // --- Empty permitted headers map ---

    @Test
    void resolve_emptyPermittedMap_returnsFallbackValueForEveryColumn() {
        HeaderResolver resolver = new HeaderResolver(Map.of());

        List<String> resolved = resolver.resolve(
                List.of(List.of("車名", "通称名")),
                List.of(0, 1)
        );

        assertThat(resolved).containsExactly("車名", "通称名");
    }

    // --- Column index out of row bounds ---

    @Test
    void resolve_columnIndexBeyondRowWidth_returnsEmptyString() {
        List<List<String>> headerRows = List.of(
                List.of("車名")  // only 1 column in row
        );
        HeaderResolver resolver = new HeaderResolver(HEADERS);

        List<String> resolved = resolver.resolve(headerRows, List.of(0, 5));

        assertThat(resolved.get(0)).isEqualTo("Car Name");
        assertThat(resolved.get(1)).isEmpty();
    }
}
