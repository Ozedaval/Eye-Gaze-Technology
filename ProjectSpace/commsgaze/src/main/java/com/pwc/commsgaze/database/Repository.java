package com.pwc.commsgaze.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class Repository {
    private ContentDAO contentDAO;
    private LiveData<List<Content>> contents;

    public Repository(Application application){
        ContentDatabase contentDatabase = ContentDatabase.getDBInstance(application);
        contentDAO = contentDatabase.contentDAO();
        contents = contentDAO.getAllContent();
    }

    public void insertContent(Content content) { new InsertAsyncTask(contentDAO).execute(content); }


    public LiveData<List<Content>> getAllContents() {
        return contents;
    }



    private static class InsertAsyncTask extends AsyncTask<Content, Void, Void> {
        private ContentDAO contentDAO;
        InsertAsyncTask(ContentDAO contentDAO) {
            this.contentDAO = contentDAO;
        }

        @Override
        protected Void doInBackground(Content... contents) {
            contentDAO.insertContent(contents[0]);
            return null;
        }
    }
}
