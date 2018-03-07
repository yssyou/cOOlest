package com.example.rayku.coolest.root;

import android.app.Application;

import com.example.rayku.coolest.MVP.ListFragmentModule;

public class App extends Application{

    private AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .listFragmentModule(new ListFragmentModule())
                .build();
    }

    public AppComponent getComponent() {
        return component;
    }
}
