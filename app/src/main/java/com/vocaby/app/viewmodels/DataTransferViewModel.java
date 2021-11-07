package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.room.rxjava3.EmptyResultSetException;

import com.vocaby.app.R;
import com.vocaby.app.data.VocabyRepository;
import com.vocaby.app.utils.Logger;
import com.vocaby.app.utils.SingleLiveEvent;
import com.vocaby.app.utils.exception.IllegalFileException;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class DataTransferViewModel extends AndroidViewModel {
    public static final int EXPORT_SAVE = 0;
    public static final int EXPORT_SAVE_BACKUP = 1;
    public static final int IMPORT_SAVE = 2;
    public static final int IMPORT_ENTRY = 3;

    private final VocabyRepository vocabyRepository;
    private final CompositeDisposable compositeDisposable;

    private final SingleLiveEvent<Boolean> mTransferSuccessful;
    private final SingleLiveEvent<Integer> mProgressText;

    private int actionType;

    public DataTransferViewModel(@NonNull Application application) {
        super(application);

        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();

        mProgressText = new SingleLiveEvent<>();
        mTransferSuccessful = new SingleLiveEvent<>();
        actionType = -1;
    }


    public LiveData<Boolean> getTransferStatus() {
        return mTransferSuccessful;
    }

    public LiveData<Integer> getProgressText() {
        return mProgressText;
    }

    public Intent handleReceived(Intent received) {
        actionType = received.getIntExtra("TYPE", -1);
        Intent intent;

        if (actionType == EXPORT_SAVE) {
            intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TITLE, "vocaby_saves.txt");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else if (actionType == EXPORT_SAVE_BACKUP) {
            intent  = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "vocaby_saves_backup.json");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else if (actionType == IMPORT_SAVE) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/json");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            intent = new Intent();
        }

        return intent;
    }

    public void handleResult(Intent result) {
        if (result != null && actionType != -1) {
            Uri uri = result.getData();
            switch (actionType) {
                case EXPORT_SAVE:
                    writeSaves(uri);
                    break;
                case EXPORT_SAVE_BACKUP:
                    writeSavesForBackup(uri);
                    break;
                case IMPORT_SAVE:
                    importSaves(uri);
                    break;
                default:
                    Logger.reportErrorToDebug(new Throwable("How did I get here"));
            }
        }
    }

    public Intent addResult() {
        return new Intent().putExtra("TYPE", actionType);
    }

    private void importSaves(Uri uri) {
        compositeDisposable.add(
                vocabyRepository.importSavesFromExternalStorage(uri)
                        .subscribe(() -> {
                            mTransferSuccessful.setValue(true);
                            mProgressText.setValue(R.string.data_transfer_import_complete);
                        }, error -> {
                            mTransferSuccessful.setValue(false);
                            if (error instanceof IllegalFileException){
                                int resultCode = ((IllegalFileException) error).getCode();
                                if (resultCode == IllegalFileException.INVALID_FORMAT) {
                                    mProgressText.setValue(R.string.data_transfer_import_error_invalid_format);
                                } else if (resultCode == IllegalFileException.INVALID_FILE) {
                                    mProgressText.setValue(R.string.data_transfer_import_error_invalid_file);
                                }
                            } else {
                                mProgressText.setValue(R.string.data_transfer_import_error_generic);
                                Logger.reportToDebug(error.getMessage());
                                Logger.reportToDebug(error.getClass().toString());
                            }
                        })
        );
    }

    private void writeSavesForBackup(Uri uri) {
        compositeDisposable.add(
                vocabyRepository.getUserSaves()
                        .flatMapCompletable(saves -> {
                            if (saves.isEmpty()) {
                                throw new EmptyResultSetException("");
                            } else {
                                mProgressText.setValue(R.string.data_transfer_exporting_backup);
                                return vocabyRepository.writeSavesJsonToExternalStorage(saves, uri);
                            }
                        })
                        .subscribe(() -> {
                            mTransferSuccessful.setValue(true);
                            mProgressText.setValue(R.string.data_transfer_export_complete);
                        }, error -> {
                            if (error instanceof EmptyResultSetException) {
                                mTransferSuccessful.setValue(false);
                                mProgressText.setValue(R.string.data_transfer_export_empty);
                            }
                        })
        );
    }

    private void writeSaves(Uri uri) {
        mProgressText.setValue(R.string.data_transfer_fetching_data);
        compositeDisposable.add(
                vocabyRepository.getUserSaves()
                    .flatMapCompletable(saves -> {
                        if (saves.isEmpty()) {
                            throw new EmptyResultSetException("");
                        } else {
                            mProgressText.setValue(R.string.data_transfer_exporting_saves);
                            return vocabyRepository.writeSavesToExternalStorage(saves, uri);
                        }
                    })
                    .subscribe(() -> {
                        mTransferSuccessful.setValue(true);
                        mProgressText.setValue(R.string.data_transfer_export_complete);
                    }, error -> {
                        if (error instanceof EmptyResultSetException) {
                            mTransferSuccessful.setValue(false);
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
