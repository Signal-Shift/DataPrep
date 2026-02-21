package com.originspecs.dataprep.model;

/**
 * Represents a single entry from permittedHeaders.csv.
 * Maps a Japanese source header label to its English output label.
 */
public record PermittedHeader(String japanese, String english) {
}
