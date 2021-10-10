package com.vocaby.app.repositories;

import android.app.Application;

import com.vocaby.app.api.ApiManager;
import com.vocaby.app.api.VocabyApiService;
import com.vocaby.app.data.DataManager;
import com.vocaby.app.data.VocabyDatabase;
import com.vocaby.app.data.dao.VocabyDao;
import com.vocaby.app.data.entity.CustomDefinition;
import com.vocaby.app.data.entity.CustomEntry;
import com.vocaby.app.data.entity.CustomEntryGroup;
import com.vocaby.app.data.entity.Definition;
import com.vocaby.app.data.entity.EntryGroupWithDefinitions;
import com.vocaby.app.data.entity.EntryWithData;
import com.vocaby.app.data.entity.User;
import com.vocaby.app.data.entity.UserSaves;
import com.vocaby.app.data.entity.WordDefinitions;
import com.vocaby.app.models.DefinitionChanges;
import com.vocaby.app.models.DefinitionGroupModel;
import com.vocaby.app.models.DefinitionModel;
import com.vocaby.app.models.EntryModel;
import com.vocaby.app.models.GroupChanges;
import com.vocaby.app.models.EntryDataPackage;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class VocabyRepository {
    private final ApiManager apiManager;
    private final VocabyDao vocabyDao;
    private final DataManager dataManager;


    public VocabyRepository(Application application) {
        VocabyDatabase vocabyDatabase = VocabyDatabase.getDatabase(application);
        vocabyDao = vocabyDatabase.vocabyDao();
        apiManager = ApiManager.getInstance();
        dataManager = DataManager.getInstance(application);
    }

    // HISTORY
    public List<String> getHistory() {
        return dataManager.getHistory();
    }

    public List<String> writeHistory(String word) {
        return dataManager.writeHistory(word);
    }

    // Gets data from UI Thread
    public Single<List<String>> getDictionaryEntriesFromDB() {
        return vocabyDao.getDictionaryEntries()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<String>> populateDictionaryEntries() {
        return dataManager.populateDictionaryEntries();
    }

    public List<String> getDictionaryEntries() {
        return dataManager.getDictionaryEntries();
    }

    public Single<List<String>> getDictionaryEntriesByLetter(String letter) {
        return vocabyDao.getDictionaryEntriesByLetter(letter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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

    public Single<EntryModel> getEntryData(int userId, String entry) {
        return vocabyDao.getUserEntryData(userId, entry)
                .map(this::convertEntryData)
                .onErrorReturnItem(new EntryModel(entry))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<EntryDataPackage> getWordDataPackageLocally(String word, int userId) {
        return Single.zip(
                getWordDataFromDatabase(word),
                getEntryData(userId, word),
                hasSave(word, userId),
                EntryDataPackage::new
        );
    }

    private EntryModel covertToEntryModel(WordDefinitions wordDefinitions) {
        EntryModel wordData = new EntryModel(wordDefinitions.word.getId(), wordDefinitions.word.getWord());
        if (wordDefinitions.word.getPronunciation() != null) {
            wordData.setPronunciation(wordDefinitions.word.getPronunciation());
        } else {
            wordData.setPronunciation("");
        }

        for (Definition data : wordDefinitions.definitions) {
            wordData.addDefinition(data.getPos(), data.getDefinition(), data.getSentence());
        }

        return wordData;
    }

    public Single<Integer> hasSave(String word, int id) {
        return vocabyDao.hasSave(word, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<EntryModel> getRandomWord() {
        return vocabyDao.getRandomWord()
                .map(this::covertToEntryModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable addSave(UserSaves userSaves) {
        return vocabyDao.addSave(userSaves)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable removeSave(int id, String word) {
        return vocabyDao.removeSave(id, word)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<User> getUser(int id) {
        return vocabyDao.getCurrentUser(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Long>> insertSavedWords(List<UserSaves> userSaves) {
        return vocabyDao.insertSavedWords(userSaves)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<String>> getUserSaves(int id) {
        // Main Thread
        return vocabyDao.getSaves(id)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable addUserSaves(List<UserSaves> saves) {
        return vocabyDao.addSaves(saves)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable removeUserSaves(List<String> saves) {
        return vocabyDao.removeSaves(saves)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<String> getRandomUserSave(int id) {
        return vocabyDao.getRandomSave(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Long> createUser(User user) {
        return vocabyDao.createUser(user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable deleteUser(User user) {
        return vocabyDao.removeUser(user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable deleteAllUsers(int localId) {
        return vocabyDao.removeAllUsers(localId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public VocabyApiService getVocabyApiService(int type) {
        return apiManager.getVocabyApiService(type);
    }

    // CUSTOM USER ENTRIES
    public Completable deleteUserEntry(int entryId, String entry) {
        return vocabyDao.deleteUserEntry(entryId)
                .andThen(Completable.fromAction(() -> dataManager.deleteEntryFromDictionary(entry)))
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

    public Single<Integer> insertOrUpdateEntry(int userId, String entry, GroupChanges groupChanges, Map<String, DefinitionChanges> definitionChangesMap) {
        Single<Long> entryInsert = Single.just((long) groupChanges.getEntryId());

        if (groupChanges.getEntryId() == -1) {
            entryInsert = vocabyDao.insertCustomEntry(new CustomEntry(userId, entry, OffsetDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()))
                    .flatMap(id -> Single.fromCallable(() -> {
                        dataManager.addEntryToDictionary(entry);
                        return id;
                    }));
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

    public Single<Long> insertCustomEntry(int userId, String entry, long date) {
        return vocabyDao.insertCustomEntry(new CustomEntry(userId, entry, date))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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

    public Single<List<String>> getUserEntries(int userId) {
        return vocabyDao.getUserEntries(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private EntryModel convertEntryData(EntryWithData data) {
        EntryModel entryData = new EntryModel(
                data.customEntry.getEntryId(),
                data.customEntry.getEntry()
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

    // UTIL
    public Single<Integer> getSavesCount() {
        return vocabyDao.getSavesCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Integer> getUserCount() {
        return vocabyDao.getUserCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
