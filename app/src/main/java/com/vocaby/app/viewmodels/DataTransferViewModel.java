package com.vocaby.app.viewmodels;

import static com.vocaby.app.ui.profile.DataManagementFragment.EXPORT_SAVE;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.room.rxjava3.EmptyResultSetException;

import com.vocaby.app.R;
import com.vocaby.app.data.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class DataTransferViewModel extends AndroidViewModel {
    private final VocabyRepository vocabyRepository;
    private final CompositeDisposable compositeDisposable;

    private final SingleLiveEvent<Integer> mProgressMax;
    private final SingleLiveEvent<Boolean> mProgressIncrement;
    private final SingleLiveEvent<Integer> mProgressText;

    private int actionType;

    public DataTransferViewModel(@NonNull Application application) {
        super(application);

        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();

        mProgressMax = new SingleLiveEvent<>();
        mProgressText = new SingleLiveEvent<>();
        mProgressIncrement = new SingleLiveEvent<>();
        actionType = -1;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public LiveData<Integer> getProgressMax() {
        return mProgressMax;
    }

    public LiveData<Boolean> getProgressIncrement() {
        return mProgressIncrement;
    }

    public LiveData<Integer> getProgressText() {
        return mProgressText;
    }

    public void handleResult(Intent result) {
        if (result != null && actionType != -1) {
            Uri uri = result.getData();
            if (actionType == EXPORT_SAVE) {
                writeSaves(uri);
            }
        }
    }

    private void writeSaves(Uri uri) {
        mProgressText.setValue(R.string.data_transfer_fetching_data);
        compositeDisposable.add(
                vocabyRepository.getUserSaves()
                    .flatMapCompletable(saves -> {
                        mProgressIncrement.setValue(true);

                        if (saves.isEmpty()) {
                            throw new EmptyResultSetException("");
                        } else {
                            mProgressText.setValue(R.string.data_transfer_exporting_saves);
                            mProgressMax.setValue(saves.size());
                            return vocabyRepository.writeSavesToExternalStorage(saves, uri);
                        }
                    })
                    .subscribe(() -> {
                        mProgressIncrement.setValue(true);
                        mProgressText.setValue(R.string.data_transfer_export_complete);
                    }, error -> {
                        if (error instanceof EmptyResultSetException) {
                            mProgressIncrement.setValue(false);
                            mProgressText.setValue(R.string.data_transfer_export_empty);
                        }
                    })
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
