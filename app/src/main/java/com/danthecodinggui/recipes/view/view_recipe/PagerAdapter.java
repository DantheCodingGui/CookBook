package com.danthecodinggui.recipes.view.view_recipe;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Manages the transitions between fragment tabs
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    private int tabsNo;

    private static final int INGREDIENTS_TAB = 0;
    private static final int METHOD_TAB = 1;

    public PagerAdapter(FragmentManager fm, int tabsNo) {
        super(fm);
        this.tabsNo = tabsNo;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case INGREDIENTS_TAB:
                return new IngredientsTabFragment();
            case METHOD_TAB:
                return new MethodTabFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabsNo;
    }
}
