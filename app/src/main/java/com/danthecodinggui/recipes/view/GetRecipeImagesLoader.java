package com.danthecodinggui.recipes.view;

import android.content.Context;
import android.os.Handler;

public class GetRecipeImagesLoader extends UpdatingAsyncTaskLoader {

    public GetRecipeImagesLoader(Context context, Handler uiThread,
                                 ProgressUpdateListener updateCallback, int loaderId) {
        super(context, uiThread, updateCallback, loaderId);
    }

    @Override
    public Object loadInBackground() {
        //should take in both the records and filepaths, complete the record and
        return null;
    }
}
