package com.originspecs.dataprep.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Config} argument parsing.
 *
 * Note: {@code Config.validate()} checks the input file exists on disk, so
 * we test only {@code Config.fromArgs()} here, which is pure parsing logic.
 */
class ConfigTest {

    private static final String VALID_INPUT  = "input.xls";
    private static final String VALID_OUTPUT = "output.xls";
    private static final String VALID_THRESHOLD = "0.05";

    // --- Happy path ---

    @Test
    void fromArgs_validArguments_createsConfigWithCorrectValues() {
        Config config = Config.fromArgs(new String[]{VALID_INPUT, VALID_OUTPUT, VALID_THRESHOLD});

        assertThat(config.inputFile().toString()).isEqualTo(VALID_INPUT);
        assertThat(config.outputFile().toString()).isEqualTo(VALID_OUTPUT);
        assertThat(config.columnThreshold()).isEqualTo(0.05);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.0", "1.0", "0.5", "0.01"})
    void fromArgs_boundaryAndTypicalThresholds_areAccepted(String threshold) {
        Config config = Config.fromArgs(new String[]{VALID_INPUT, VALID_OUTPUT, threshold});

        assertThat(config.columnThreshold()).isBetween(0.0, 1.0);
    }

    // --- Wrong argument count ---

    @Test
    void fromArgs_tooFewArguments_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> Config.fromArgs(new String[]{VALID_INPUT, VALID_OUTPUT}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3 arguments required");
    }

    @Test
    void fromArgs_tooManyArguments_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> Config.fromArgs(new String[]{VALID_INPUT, VALID_OUTPUT, VALID_THRESHOLD, "extra"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3 arguments required");
    }

    @Test
    void fromArgs_noArguments_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> Config.fromArgs(new String[]{}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- Invalid threshold ---

    @ParameterizedTest
    @ValueSource(strings = {"-0.1", "1.1", "2.0", "-1.0"})
    void fromArgs_thresholdOutOfRange_throwsIllegalArgumentException(String threshold) {
        assertThatThrownBy(() -> Config.fromArgs(new String[]{VALID_INPUT, VALID_OUTPUT, threshold}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("columnThreshold");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "", "0.1x", "NaN"})
    void fromArgs_thresholdNotNumeric_throwsIllegalArgumentException(String threshold) {
        assertThatThrownBy(() -> Config.fromArgs(new String[]{VALID_INPUT, VALID_OUTPUT, threshold}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("columnThreshold");
    }
}
