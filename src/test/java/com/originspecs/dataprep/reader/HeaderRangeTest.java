package com.originspecs.dataprep.reader;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HeaderRange}.
 * Covers row classification and data-start index derivation.
 */
class HeaderRangeTest {

    // Range covering rows 3–7 (typical MLIT sheet layout)
    private final HeaderRange range = new HeaderRange(3, 7);

    // --- dataStartRowIndex ---

    @Test
    void dataStartRowIndex_isOneAfterEndRow() {
        assertThat(range.dataStartRowIndex()).isEqualTo(8);
    }

    @Test
    void dataStartRowIndex_singleRowHeader_isNextRow() {
        HeaderRange single = new HeaderRange(0, 0);
        assertThat(single.dataStartRowIndex()).isEqualTo(1);
    }

    // --- isHeaderRow ---

    @Test
    void isHeaderRow_trueForStartRow() {
        assertThat(range.isHeaderRow(3)).isTrue();
    }

    @Test
    void isHeaderRow_trueForEndRow() {
        assertThat(range.isHeaderRow(7)).isTrue();
    }

    @Test
    void isHeaderRow_trueForMiddleRow() {
        assertThat(range.isHeaderRow(5)).isTrue();
    }

    @Test
    void isHeaderRow_falseForRowBeforeStart() {
        assertThat(range.isHeaderRow(2)).isFalse();
    }

    @Test
    void isHeaderRow_falseForRowAfterEnd() {
        assertThat(range.isHeaderRow(8)).isFalse();
    }

    // --- isPreHeaderRow ---

    @Test
    void isPreHeaderRow_trueForRowBeforeStart() {
        assertThat(range.isPreHeaderRow(0)).isTrue();
        assertThat(range.isPreHeaderRow(2)).isTrue();
    }

    @Test
    void isPreHeaderRow_falseForStartRow() {
        assertThat(range.isPreHeaderRow(3)).isFalse();
    }

    @Test
    void isPreHeaderRow_falseForRowsInsideOrAfterRange() {
        assertThat(range.isPreHeaderRow(5)).isFalse();
        assertThat(range.isPreHeaderRow(10)).isFalse();
    }
}
