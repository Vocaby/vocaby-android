package com.vocaby.app.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EntryModel {
    private final String word;
    private String pronunciation;
    private final Map<String, List<DefinitionModel>> mDefinitions;
    private final List<String> types;

    public EntryModel(String word) {
        this.word = word;
        pronunciation = "";
        types = new ArrayList<>();
        mDefinitions = new HashMap<>();
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getPronunciation() { return this.pronunciation; }

    public String getWord() { return word; }

    public boolean isEmpty() {
        return mDefinitions.isEmpty();
    }

    public void setDefinitions(String type, List<DefinitionModel> newDefinitions) {
        if (mDefinitions.containsKey(type)) {
            mDefinitions.replace(type, newDefinitions);
        } else {
            types.add(type);
            mDefinitions.put(type, newDefinitions);
        }
    }

    public void removeDefinitions(String type) {
        mDefinitions.remove(type);
    }

    public void addDefinition(String type, String definition, List<String> examples) {
        if(mDefinitions.containsKey(type)) {
            List<DefinitionModel> definitions = mDefinitions.get(type);
            if (definitions != null) {
                definitions.add(new DefinitionModel(definition, type, examples));
            }
        } else {
            types.add(type);
            List<DefinitionModel> definitions = new LinkedList<>();
            definitions.add(new DefinitionModel(definition, type, examples));
            mDefinitions.put(type, definitions);
        }
    }

    public void addDefinition(String type, String definition, String example) {
        if(mDefinitions.containsKey(type)) {
            List<DefinitionModel> definitions = mDefinitions.get(type);
            if (definitions != null) {
                definitions.add(new DefinitionModel(definition, type, example));
            }
        } else {
            types.add(type);
            List<DefinitionModel> definitions = new LinkedList<>();
            definitions.add(new DefinitionModel(definition, type, example));
            mDefinitions.put(type, definitions);
        }
    }

    public List<String> getTypes() {
        return types;
    }

    public String getFirstType() {
        if (types.size() == 0) {
            return "";
        } else {
            return types.get(0);
        }
    }

    public List<DefinitionModel> getDefinitions(String pos) {
        return mDefinitions.get(pos);
    }
}
