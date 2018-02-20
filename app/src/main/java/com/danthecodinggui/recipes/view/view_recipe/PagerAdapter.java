package com.danthecodinggui.recipes.view.view_recipe;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the transitions between fragment tabs
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    private int tabsNo;

    private List<String> tabTitles;

    private static final int INGREDIENTS_TAB = 0;
    private static final int METHOD_TAB = 1;

    PagerAdapter(FragmentManager fm, int tabsNo, List<String> tabTitles) {
        super(fm);
        this.tabsNo = tabsNo;
        this.tabTitles = tabTitles;
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

    @Override
    public CharSequence getPageTitle(int position) {
            return tabTitles.get(position);
    }
}
