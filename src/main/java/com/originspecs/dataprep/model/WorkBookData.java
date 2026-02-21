package com.originspecs.dataprep.model;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class WorkBookData {

    private String fileName;
    private List<WorkSheetData> worksheets = new ArrayList<>();
    private int worksheetCount;

}
