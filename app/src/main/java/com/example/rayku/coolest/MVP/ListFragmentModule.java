package com.example.rayku.coolest.MVP;

import dagger.Module;
import dagger.Provides;

@Module
public class ListFragmentModule {

    // not sure yet
    @Provides
    public MainActivityMVP.Presenter providePresenter(){
        return new ListFragmentPresenter();
    }


}
