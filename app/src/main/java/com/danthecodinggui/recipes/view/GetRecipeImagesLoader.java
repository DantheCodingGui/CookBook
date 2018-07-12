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
        return null;
    }
}
