package com.vocaby.app.viewmodels;

import static com.vocaby.app.Constants.ITEM_PAYLOAD_KEY;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vocaby.app.models.ItemIntPayload;
import com.vocaby.app.models.ItemState;
import com.vocaby.app.models.ItemStringPayload;
import com.vocaby.app.data.VocabyRepository;
import com.vocaby.app.utils.Logger;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class MyEntryViewModel extends AndroidViewModel {
    private List<String> customEntries;

    private  final VocabyRepository vocabyRepository;
    private final CompositeDisposable compositeDisposable;

    private int selectedPosition;

    private final MutableLiveData<Integer> mEntryCount;
    private final SingleLiveEvent<ItemIntPayload> mEntryBuilderResultPayload;
    private final SingleLiveEvent<List<String>> mEntries;
    private final SingleLiveEvent<ItemIntPayload> mDeleteStatus;

    public MyEntryViewModel(@NonNull Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();

        selectedPosition = -1;

        mEntryCount = new MutableLiveData<>(0);
        mEntryBuilderResultPayload = new SingleLiveEvent<>();
        mEntries = new SingleLiveEvent<>();
        mDeleteStatus = new SingleLiveEvent<>();

        compositeDisposable.add(
                vocabyRepository.getCurrentUserId()
                .flatMap(vocabyRepository::getUserEntries)
                .subscribe(list -> {
                    customEntries = new ArrayList<>(list);
                    mEntries.setValue(customEntries);
                    mEntryCount.setValue(customEntries.size());
                }, Logger::reportError)
        );
    }

    public LiveData<List<String>> getEntries() {
        return mEntries;
    }
    public LiveData<ItemIntPayload> getEntryResultPayload() { return mEntryBuilderResultPayload; }
    public LiveData<Integer> getCustomEntryCount() {
        return mEntryCount;
    }
    public LiveData<ItemIntPayload> getDeleteStatus() { return mDeleteStatus; }

    public Intent addEntryDataToIntent(Intent intent, String entry, int position) {
        int selectedPosition;
        ItemStringPayload itemStringPayload = new ItemStringPayload(entry);
        if (position == -1) {
            selectedPosition = customEntries.indexOf(entry);
        } else {
            selectedPosition = position;
        }

        if (selectedPosition == -1) {
            itemStringPayload.setState(ItemState.ADD);
        } else {
            itemStringPayload.setState(ItemState.UPDATE);
        }

        intent.putExtra(ITEM_PAYLOAD_KEY, itemStringPayload);
        return intent;
    }

    public void handleResult(ActivityResult result) {
        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
            ItemStringPayload receivedPayload =
                    result.getData().getParcelableExtra(ITEM_PAYLOAD_KEY);

            if (receivedPayload.getState() == ItemState.ADD) {
                customEntries.add(0, receivedPayload.getPayload());
            } else if (receivedPayload.getState() == ItemState.DELETE){
                if (selectedPosition != -1 ) customEntries.remove(selectedPosition);
                else customEntries.remove(receivedPayload.getPayload());
            }

            mEntryBuilderResultPayload.setValue(new ItemIntPayload(receivedPayload.getState(), selectedPosition));
            mEntryCount.setValue(customEntries.size());
        }
    }

    public void removeCustomEntry(String entry, int position) {
        compositeDisposable.add(
                vocabyRepository.deleteUserEntry(entry)
                    .subscribe(() -> {
                        customEntries.remove(position);
                        mDeleteStatus.setValue(new ItemIntPayload(ItemState.DELETE, position));
                        mEntryCount.setValue(customEntries.size());
                    }, error -> {
                        mDeleteStatus.setValue(new ItemIntPayload(ItemState.UNCHANGED, position));
                        Logger.reportError(error);
                    })
        );
    }

    public void setSelectedPosition(int selected) {
        selectedPosition = selected;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
