package com.vocaby.app.viewmodels;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.room.rxjava3.EmptyResultSetException;

import com.vocaby.app.R;
import com.vocaby.app.data.VocabyRepository;
import com.vocaby.app.models.dictionary.EntryModel;
import com.vocaby.app.models.viewstate.SaveStateModel;
import com.vocaby.app.utils.Logger;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class SearchResultsViewModel extends AndroidViewModel {
    private final CompositeDisposable compositeDisposable;
    private final VocabyRepository vocabyRepository;

    private final SingleLiveEvent<List<EntryModel>> mEntryData;
    private final SingleLiveEvent<Integer> mDictionaryMissing;
    private final SingleLiveEvent<SaveStateModel> mSaveState;

    public SearchResultsViewModel(@NonNull Application application) {
        super(application);
        compositeDisposable = new CompositeDisposable();
        vocabyRepository = new VocabyRepository(application);

        mEntryData = new SingleLiveEvent<>();
        mDictionaryMissing = new SingleLiveEvent<>();
        mSaveState = new SingleLiveEvent<>();

        SaveStateModel saveState = new SaveStateModel(
                View.VISIBLE,
                R.drawable.ic_bookmark_disabled,
                R.drawable.ic_bookmark_unsaved,
                R.drawable.ic_bookmark_saved,
                R.string.save_button_unsaved,
                R.string.save_button_saved,
                R.color.gray,
                R.color.colorPrimary,
                false,
                false
        );

        mSaveState.setValue(saveState);
    }

    public LiveData<List<EntryModel>> getWordData() { return mEntryData; }
    public LiveData<Integer> getDictionaryMissing() { return mDictionaryMissing; }
    public LiveData<SaveStateModel> getSaveState() { return mSaveState; }

    public void retrieveWordDataFromRepo(String searched) {
        compositeDisposable.add(
                vocabyRepository.getWordDataPackageLocally(searched)
                    .subscribe(wordPackage -> {
                        // Setting save state
                        SaveStateModel saveState = mSaveState.getValue();
                        if (saveState != null) {
                            saveState.setSaved(wordPackage.saved());
                            saveState.setEnabled(true);
                            mSaveState.setValue(saveState);
                        }

                        List<EntryModel> entryData = new ArrayList<>();
                        if (wordPackage.bothDataAvailable()) {
                            entryData.add(wordPackage.getCustomData());
                            entryData.add(wordPackage.getOriginalData());
                        } else if (wordPackage.onlyCustomAvailable()) {
                            entryData.add(wordPackage.getCustomData());
                            mDictionaryMissing.setValue(R.id.selection_original);
                        } else {
                            entryData.add(wordPackage.getOriginalData());
                            mDictionaryMissing.setValue(R.id.selection_custom);

                            if (wordPackage.getOriginalData().isEmpty() && saveState != null) {
                                saveState.setVisibility(View.GONE);
                                mSaveState.setValue(saveState);
                            }
                        }

                        mEntryData.setValue(entryData);
                    }, error -> {
                        if (error instanceof EmptyResultSetException) {
                            List<EntryModel> empty = new ArrayList<>();
                            empty.add(new EntryModel(searched));
                            mEntryData.setValue(empty);
                        }

                        Logger.reportError(error);
                    }
            )
        );
    }

    public void updateEntrySave(String entry) {
        if (mSaveState.getValue() != null) {
            SaveStateModel saveState = mSaveState.getValue();
            saveState.setEnabled(false);
            mSaveState.setValue(saveState);

            if (saveState.getSaved()) {
                compositeDisposable.add(
                        vocabyRepository.removeSave(entry)
                            .subscribe(() -> setUnSaved(saveState),
                            error -> {
                                setSaved(saveState);
                                Logger.reportError(error);
                            }
                        )
                );
            } else {
                compositeDisposable.add(
                        vocabyRepository.addSave(entry)
                            .subscribe(() -> setSaved(saveState),
                                error -> {
                                setUnSaved(saveState);
                                Logger.reportError(error);
                            })
                );
            }
        }
    }

    private void setSaved(SaveStateModel saveState) {
        saveState.setEnabled(true);
        saveState.setSaved(true);
        mSaveState.setValue(saveState);
    }

    private void setUnSaved(SaveStateModel saveState) {
        saveState.setEnabled(true);
        saveState.setSaved(false);
        mSaveState.setValue(saveState);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
