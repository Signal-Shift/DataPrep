package com.originspecs.dataprep.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RowData {

    private List<String> cellValues = new ArrayList<>();

    public String getCell(int index) {
        return index < cellValues.size() ? cellValues.get(index) : "";
    }

    public int size() {
        return cellValues.size();
    }
}
