package com.vocaby.app.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.vocaby.app.data.VocabyRepository;
import com.vocaby.app.utils.Logger;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class DangerZoneViewModel extends AndroidViewModel {
    private VocabyRepository vocabyRepository;
    private CompositeDisposable compositeDisposable;

    public DangerZoneViewModel(@NonNull Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();
    }

    public void clearHistory() {

    }

    public void clearSaves() {
        compositeDisposable.add(
                vocabyRepository.clearSaves()
                .subscribe(() -> {
                    Log.d("vocabydebug", "clearSaves: done");
                }, Logger::reportError)
        );
    }

    public void clearEntries() {

    }

    public void eraseAllData() {

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
