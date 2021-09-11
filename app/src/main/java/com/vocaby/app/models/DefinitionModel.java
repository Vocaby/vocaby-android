package com.vocaby.app.models;

public class DefinitionModel {
    private String definition;
    private String example;

    public DefinitionModel(String definition, String example) {
        this.definition = definition;
        this.example = example;
    }

    public DefinitionModel(String definition) {
        this.definition = definition;
        this.example = "";
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }
}
