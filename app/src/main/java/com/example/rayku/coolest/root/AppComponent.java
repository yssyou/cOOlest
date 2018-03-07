package com.example.rayku.coolest.root;

import com.example.rayku.coolest.MVP.ListFragmentModule;
import com.example.rayku.coolest.MVP.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, ListFragmentModule.class})
public interface AppComponent {

    void inject(MainActivity mainActivity);

}
