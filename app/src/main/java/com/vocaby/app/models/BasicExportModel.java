package com.vocaby.app.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import static com.vocaby.app.Constants.EXPORT_FILE_TYPE_FIELD;

public class BasicExportModel<T> {

    @SerializedName(value = EXPORT_FILE_TYPE_FIELD)
    private final String vocabyExportType;
    private final List<T> data;

    public BasicExportModel(String vocabyExportType, List<T> data) {
        this.vocabyExportType = vocabyExportType;
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }
}
