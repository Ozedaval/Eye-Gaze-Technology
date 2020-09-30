package com.pwc.commsgaze;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.pwc.commsgaze.database.Content;
import com.pwc.commsgaze.database.Repository;
import java.util.List;

public class StorageViewModel extends AndroidViewModel {
    private Repository repository;
    private LiveData<List<Content>> contents;

    public StorageViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        contents = repository.getAllContents();
    }

    LiveData<List<Content>> getAllContents() { return contents; }


    public void insertContent(Content content) { repository.insertContent(content); }

}
