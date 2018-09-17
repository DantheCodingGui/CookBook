package com.danthecodinggui.recipes.view.view_recipe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import static com.danthecodinggui.recipes.msc.IntentConstants.RECIPE_DETAIL_ID;

/**
 * Manages the transitions between fragment tabs
 */
public class RecipePagerAdapter extends FragmentStatePagerAdapter {

    private int tabsNo;

    private List<String> tabTitles;

    private static final int INGREDIENTS_TAB = 0;
    private static final int METHOD_TAB = 1;

    private long recipeId;

    RecipePagerAdapter(FragmentManager fm, int tabsNo, List<String> tabTitles, long recipeId) {
        super(fm);
        this.tabsNo = tabsNo;
        this.tabTitles = tabTitles;
        this.recipeId = recipeId;
    }

    @Override
    public Fragment getItem(int position) {

        Bundle recipeBundle = new Bundle();
        recipeBundle.putLong(RECIPE_DETAIL_ID, recipeId);

        switch (position) {
            case INGREDIENTS_TAB:
                IngredientsTabFragment ingFrag = new IngredientsTabFragment();
                ingFrag.setArguments(recipeBundle);
                return ingFrag;
            case METHOD_TAB:
                MethodTabFragment methFrag = new MethodTabFragment();
                methFrag.setArguments(recipeBundle);
                return methFrag;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabsNo;
    }

    @Override
    public CharSequence getPageTitle(int position) {
            return tabTitles.get(position);
    }
}
