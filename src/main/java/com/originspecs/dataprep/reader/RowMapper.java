package com.originspecs.dataprep.reader;

import org.apache.poi.ss.usermodel.Row;

/**
 * Functional interface for mapping an Apache POI Row
 * into a domain object.
 *
 * @param <T> The target type to map the row into.
 */

@FunctionalInterface
public interface RowMapper<T> {

    //maps a single excel row into a domain object
    T map(Row row);
}
