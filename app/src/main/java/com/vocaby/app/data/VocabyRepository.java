package com.vocaby.app.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.vocaby.app.Constants;
import com.vocaby.app.data.dao.VocabyDao;
import com.vocaby.app.data.entity.CustomDefinition;
import com.vocaby.app.data.entity.CustomEntry;
import com.vocaby.app.data.entity.CustomEntryGroup;
import com.vocaby.app.data.entity.Definition;
import com.vocaby.app.data.entity.EntryGroupWithDefinitions;
import com.vocaby.app.data.entity.EntryWithData;
import com.vocaby.app.data.entity.User;
import com.vocaby.app.data.entity.UserSave;
import com.vocaby.app.data.entity.WordDefinitions;
import com.vocaby.app.models.BasicExportModel;
import com.vocaby.app.models.customentry.DefinitionChanges;
import com.vocaby.app.models.customentry.GroupChanges;
import com.vocaby.app.models.datapackage.EntryDataPackage;
import com.vocaby.app.models.dictionary.DefinitionGroupModel;
import com.vocaby.app.models.dictionary.DefinitionModel;
import com.vocaby.app.models.dictionary.EntryModel;
import com.vocaby.app.utils.Logger;
import com.vocaby.app.utils.StringFormatter;
import com.vocaby.app.utils.exception.IllegalFileException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.vocaby.app.Constants.DICTIONARY_ENTRIES_KEY;
import static com.vocaby.app.Constants.EXPORT_FILE_TYPE_FIELD;

public class VocabyRepository {
    private final Application application;
    private final VocabyDao vocabyDao;
    private final DataManager dataManager;
    private final SharedPreferences userSharedPreference;
    private final SharedPreferences entrySharedPreference;
    private int userId = 1;


    public VocabyRepository(Application application) {
        this.application = application;
        VocabyDatabase vocabyDatabase = VocabyDatabase.getDatabase(application);
        vocabyDao = vocabyDatabase.vocabyDao();
        dataManager = DataManager.Companion.getInstance(application);
        userSharedPreference = application.getSharedPreferences(Constants.USER_ID_KEY, Context.MODE_PRIVATE);
        entrySharedPreference = application.getSharedPreferences(DICTIONARY_ENTRIES_KEY, Context.MODE_PRIVATE);
    }

    public Completable setupUser() {
        return vocabyDao.checkUser(userId)
                .flatMapCompletable(result -> {
                    if (result == 0) {
                        return createUser();
                    } else {
                        return Completable.fromAction(() ->
                                userId = userSharedPreference.getInt(Constants.CURRENT_USER_ID_KEY, 1));
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    // HISTORY
    public List<String> getHistory() {
        return dataManager.getHistory();
    }

    public List<String> writeHistory(String word) {
        return dataManager.writeHistory(word);
    }

    public Single<List<String>> clearHistory() {
        return null;
    }

    public Completable importSavesFromExternalStorage(Uri uri) {
        Single<List<UserSave>> parseJson = Single.fromCallable(() -> {
            InputStream inputStream = application.getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
                List<UserSave> saves = new ArrayList<>();

                if (jsonObject.has(EXPORT_FILE_TYPE_FIELD)) {
                    if (jsonObject.getAsJsonPrimitive(EXPORT_FILE_TYPE_FIELD).getAsString().equals("saves")) {
                        for (JsonElement item : jsonObject.getAsJsonArray("data")) {
                            saves.add(new UserSave(userId, StringFormatter.cleanText(item.getAsString())));
                        }
                    } else {
                        throw new IllegalFileException(IllegalFileException.INVALID_FILE);
                    }
                } else {
                    throw new IllegalFileException(IllegalFileException.INVALID_FORMAT);
                }

                return saves;
            } catch (JsonSyntaxException error) {
                throw new IllegalFileException(IllegalFileException.INVALID_FORMAT);
            } finally {
                inputStream.close();
                reader.close();
            }
        });

        return parseJson
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapCompletable(saves -> clearSaves().andThen(addUserSaves(saves)));

    }

    public Completable writeSavesJsonToExternalStorage(List<String> saves, Uri uri) {
        return Completable.fromAction(() -> {
            try (OutputStream outputStream = application.getContentResolver().openOutputStream(uri)) {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
                Gson gson = new Gson();
                gson.toJson(new BasicExportModel<>("saves", saves), bw);

                bw.flush();
                bw.close();
            }
        }).subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable writeSavesToExternalStorage(List<String> saves, Uri uri) {
        return Completable.fromAction(() -> {
            try (OutputStream outputStream = application.getContentResolver().openOutputStream(uri)) {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
                for (int i = 0; i < saves.size(); i++) {
                    bw.write(saves.get(i));
                    bw.newLine();
                }

                bw.flush();
                bw.close();
            }
        }).subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<String>> getSortedDictionaryEntriesFromDB() {
        return vocabyDao.getDictionaryEntries()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable setupDictionaryEntries() {
        if (entrySharedPreference.contains("z")) {
            return Completable.complete();
        } else {
            return getSortedDictionaryEntriesFromDB()
                    .flatMapCompletable(list -> Completable.fromAction(() -> {
                        int i = 0;
                        StringBuilder sb = new StringBuilder();
                        for (; i < list.size()-1; i++) {
                            if (list.get(i).charAt(0) == list.get(i+1).charAt(0)) {
                                sb.append(list.get(i));
                                sb.append(";");
                            } else {
                                sb.append(list.get(i));

                                SharedPreferences.Editor editor = entrySharedPreference.edit();
                                editor.putString(list.get(i).substring(0, 1), sb.toString());
                                editor.apply();

                                sb = new StringBuilder();
                            }
                        }

                        // Last element insertion
                        sb.append(list.get(i));

                        SharedPreferences.Editor editor = entrySharedPreference.edit();
                        editor.putString(list.get(i).substring(0, 1), sb.toString());
                        editor.apply();
                    })).observeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    public Single<List<String>> getEntriesByCharacter(String character) {
        return Single.fromCallable(() -> {
            String e = entrySharedPreference.getString(character, "");
            return Arrays.asList(e.split(";"));
        });
    }

    public void addEntryToDictionary(String entry) {
        String character = entry.substring(0, 1);
        String entries = entrySharedPreference.getString(character, "");
        StringBuilder sb = new StringBuilder();
        boolean added = false;

        if (!entries.isEmpty()) {
            List<String> list = Arrays.asList(entries.split(";"));

            int i = 0;
            for (; i < list.size(); i++) {
                if (!added && entry.compareToIgnoreCase(list.get(i)) < 0) {
                    added = true;
                    sb.append(entry);
                    sb.append(";");
                }

                sb.append(list.get(i));
                sb.append(";");
            }

            if (!added) {
                sb.append(entry);
            }

            entrySharedPreference.edit().putString(character, sb.toString()).apply();
        }
    }

    public void deleteEntryFromDictionary(String entry) {
        String character = entry.substring(0, 1);
        String entries = entrySharedPreference.getString(character, "");
        StringBuilder sb = new StringBuilder();

        if (!entries.isEmpty()) {
            List<String> list = Arrays.asList(entries.split(";"));

            for (int i = 0; i < list.size(); i++) {
                if (!list.get(i).equals(entry)) {
                    sb.append(list.get(i));
                    sb.append(";");
                }
            }

            entrySharedPreference.edit().putString(character, sb.toString()).apply();
        }
    }

    public Single<EntryModel> getWordDataFromDatabase(String word) {
        return vocabyDao.getWordData(word)
                .map(this::covertToEntryModel)
                .onErrorReturnItem(new EntryModel(word))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<EntryModel> getWordDataFromDatabase(int id) {
        return vocabyDao.getWordData(id)
                .map(this::covertToEntryModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<EntryModel> getEntryData(String entry) {
        return vocabyDao.getUserEntryData(userId, entry)
                .map(this::convertEntryData)
                .onErrorReturnItem(new EntryModel(entry))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<EntryDataPackage> getWordDataPackageLocally(String word) {
        return Single.zip(
                getWordDataFromDatabase(word),
                getEntryData(word),
                EntryDataPackage::new
        );
    }

    public Single<Integer> hasSave(String word) {
        return vocabyDao.hasSave(word, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<EntryModel> getRandomWord() {
        return vocabyDao.getRandomWord()
                .map(this::covertToEntryModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable addSave(String entry) {
        return vocabyDao.addSave(new UserSave(userId, entry))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable addSaves(List<String> newSaves) {
        List<UserSave> userSaves = new ArrayList<>();
        for (String entry : newSaves) {
            userSaves.add(new UserSave(userId, entry));
        }

        return vocabyDao.addSaves(userSaves)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable addUserSaves(List<UserSave> newSaves) {
        return vocabyDao.addSaves(newSaves)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable clearSaves() {
        return vocabyDao.removeAllSaves(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable removeSave(String word) {
        return vocabyDao.removeSave(userId, word)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<String>> getUserSaves() {
        return vocabyDao.getSaves(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable createUser() {
        return vocabyDao.createUser(new User())
                .flatMapCompletable(id -> Completable.fromAction(() -> {
                    int resultId = id.intValue();
                    SharedPreferences.Editor editor = userSharedPreference.edit();
                    editor.putInt("LOCAL_USER_ID", resultId);
                    editor.putInt(Constants.CURRENT_USER_ID_KEY, resultId);
                    editor.apply();
                })).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    // CUSTOM USER ENTRIES
    public Completable clearUserEntries() {
        return vocabyDao.clearUserEntries(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable deleteUserEntry(int entryId, String entry) {
        return vocabyDao.deleteUserEntry(entryId)
                .andThen(vocabyDao.checkEntryExistence(entry))
                .flatMapCompletable(exists -> {
                    if (!exists) return Completable.fromAction(() -> deleteEntryFromDictionary(entry));
                    else return Completable.complete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable deleteUserEntry(String entry) {
        return vocabyDao.deleteUserEntry(entry)
                .andThen(vocabyDao.checkEntryExistence(entry))
                .flatMapCompletable(exists -> {
                    if (!exists) return Completable.fromAction(() -> deleteEntryFromDictionary(entry));
                    else return Completable.complete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable deleteUserEntryGroups(List<CustomEntryGroup> groups) {
        return vocabyDao.deleteCustomEntryGroups(groups)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable updateUserEntryGroups(List<CustomEntryGroup> groups) {
        return vocabyDao.updateCustomEntryGroups(groups)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Integer> insertOrUpdateEntry(String entry,
                                               String pronunciation,
                                               GroupChanges groupChanges,
                                               Map<String, DefinitionChanges> definitionChangesMap) {
        Single<Long> entryInsert;
        // New entry
        if (groupChanges.getEntryId() == -1) {
            Logger.reportToDebug("New Entry: " + userId);
            entryInsert = vocabyDao.insertCustomEntry(
                    new CustomEntry(userId,
                            entry,
                            pronunciation,
                            OffsetDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()
                    )
            ).flatMap(id ->
                    vocabyDao.checkEntryExistence(entry)
                        .flatMap(exists ->
                                Single.fromCallable(() -> {
                                    if (!exists) addEntryToDictionary(entry);
                                    return id;
                        })
                    )
            );
        } else {
            entryInsert = vocabyDao.updateCustomEntry(new CustomEntry(
                    groupChanges.getEntryId(),
                    userId,
                    entry,
                    pronunciation,
                    OffsetDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()
            )).andThen(Single.just((long)groupChanges.getEntryId()));
        }

        return entryInsert
                .flatMap(id -> {
                    int entryId = id.intValue();
                    List<CustomEntryGroup> deletedGroups = new ArrayList<>();
                    for (DefinitionGroupModel group : groupChanges.getDeletedItems()) {
                        deletedGroups.add(new CustomEntryGroup(group.getGroupId()));
                    }

                    List<CustomEntryGroup> updatedGroups = new ArrayList<>();
                    for (DefinitionGroupModel group : groupChanges.getUpdatedItems()) {
                        updatedGroups.add(new CustomEntryGroup(
                                group.getGroupId(),
                                entryId,
                                group.getType(),
                                group.getOrder()
                        ));
                    }

                    List<CustomEntryGroup> addedGroups = new ArrayList<>();
                    for (DefinitionGroupModel group : groupChanges.getAddedItems()) {
                        addedGroups.add(new CustomEntryGroup(
                                entryId,
                                group.getType(),
                                group.getOrder()
                        ));
                    }

                    List<CustomDefinition> deletedDefinitions = new ArrayList<>();
                    for (DefinitionChanges definitionChanges : definitionChangesMap.values()) {
                        for (DefinitionModel definitionModel : definitionChanges.getDeletedItems()) {
                            deletedDefinitions.add(new CustomDefinition(
                                    definitionModel.getId(),
                                    definitionChanges.getGroupId(),
                                    definitionModel.getDefinition(),
                                    definitionModel.getExample(),
                                    definitionModel.getOrder()
                            ));
                        }
                    }

                    List<CustomDefinition> updatedDefinitions = new ArrayList<>();
                    for (DefinitionChanges definitionChanges : definitionChangesMap.values()) {
                        for (DefinitionModel definitionModel : definitionChanges.getUpdatedItems()) {
                            updatedDefinitions.add(new CustomDefinition(
                                    definitionModel.getId(),
                                    definitionChanges.getGroupId(),
                                    definitionModel.getDefinition(),
                                    definitionModel.getExample(),
                                    definitionModel.getOrder()
                            ));
                        }
                    }

                    return deleteUserEntryGroups(deletedGroups)
                            .andThen(updateUserEntryGroups(updatedGroups))
                            .andThen(insertUserEntryGroups(addedGroups))
                            .flatMapCompletable(ids -> {
                                List<DefinitionGroupModel> newGroups = groupChanges.getAddedItems();
                                for (int i = 0; i < newGroups.size(); i++) {
                                    DefinitionChanges definitionChanges = definitionChangesMap.get(newGroups.get(i).getType());
                                    if (definitionChanges != null)
                                        definitionChanges.setGroupId(ids.get(i).intValue());
                                }

                                List<CustomDefinition> addedDefinitions = new ArrayList<>();
                                for (DefinitionChanges definitionChanges : definitionChangesMap.values()) {
                                    for (DefinitionModel definitionModel : definitionChanges.getAddedItems()) {
                                        addedDefinitions.add(new CustomDefinition(
                                                definitionChanges.getGroupId(),
                                                definitionModel.getDefinition(),
                                                definitionModel.getExample(),
                                                definitionModel.getOrder())
                                        );
                                    }
                                }

                                return insertUserDefinitions(addedDefinitions);
                            }).andThen(updateUserDefinitions(updatedDefinitions))
                            .andThen(deleteUserDefinitions(deletedDefinitions))
                            .andThen(Single.just(entryId));
                });
    }

    public Single<List<Long>> insertUserEntryGroups(List<CustomEntryGroup> groups) {
        return vocabyDao.insertCustomEntryGroups(groups)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable insertUserDefinitions(List<CustomDefinition> definitions) {
        return vocabyDao.insertCustomDefinitions(definitions)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable updateUserDefinitions(List<CustomDefinition> definitions) {
        return vocabyDao.updateCustomDefinitions(definitions)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable deleteUserDefinitions(List<CustomDefinition> definitions) {
        return vocabyDao.deleteCustomDefinitions(definitions)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<String>> getUserEntries() {
        return vocabyDao.getUserEntries(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private EntryModel covertToEntryModel(WordDefinitions wordDefinitions) {
        String pronunciation =
                wordDefinitions.word.getPronunciation() != null ? wordDefinitions.word.getPronunciation() : "";

        EntryModel wordData = new EntryModel(
                wordDefinitions.word.getId(),
                wordDefinitions.word.getWord(),
                pronunciation
        );

        for (Definition data : wordDefinitions.definitions) {
            wordData.addDefinition(data.getPos(), data.getDefinition(), data.getSentence());
        }

        return wordData;
    }

    private EntryModel convertEntryData(EntryWithData data) {
        String pronunciation =
                data.customEntry.getPronunciation() != null ? data.customEntry.getPronunciation() : "";

        EntryModel entryData = new EntryModel(
                data.customEntry.getEntryId(),
                data.customEntry.getEntry(),
                pronunciation
        );

        List<DefinitionGroupModel> groups = new ArrayList<>();
        for (EntryGroupWithDefinitions group : data.groups) {
            DefinitionGroupModel groupModel = new DefinitionGroupModel(
                    group.entryGroup.getGroupId(),
                    group.entryGroup.getType(),
                    group.entryGroup.getOrder()
            );

            List<DefinitionModel> definitions = new ArrayList<>();
            for (CustomDefinition definitionData : group.definitions) {
                DefinitionModel definitionModel = new DefinitionModel(
                        definitionData.getDefinitionId(),
                        group.entryGroup.getType(),
                        definitionData.getDefinition(),
                        definitionData.getExample(),
                        definitionData.getOrder()
                );

                definitions.add(definitionModel);
            }

            Collections.sort(definitions);
            groupModel.setDefinitionData(definitions);
            groups.add(groupModel);
        }

        Collections.sort(groups);
        entryData.setDefinitionGroups(groups);

        return entryData;
    }

    public Single<List<String>> getAllTypes() {
        return vocabyDao.getTypes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
