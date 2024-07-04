package com.example.globalsedentary.ui.Dummy;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DummyViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public DummyViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Generador de datos para simulación del dispositivo");
    }

    public LiveData<String> getText() {
        return mText;
    }
}