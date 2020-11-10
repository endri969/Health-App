package com.polant.projectsport.adapter.tab;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.polant.projectsport.R;
import com.polant.projectsport.fragment.article.ArticleFragment;

import java.util.ArrayList;


public class TabsArticlesFragmentAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> tabs;

    public TabsArticlesFragmentAdapter(Activity activity, FragmentManager fm) {
        super(fm);

        tabs = new ArrayList<>();
        tabs.add(activity.getString(R.string.tab_sport_article));
        tabs.add(activity.getString(R.string.tab_food_article));
        tabs.add(activity.getString(R.string.tab_fitness_article));
    }

    @Override
    public Fragment getItem(int position) {
        return ArticleFragment.getInstance(tabs.get(position));
    }

    @Override
    public int getItemPosition(Object object) {
        ArticleFragment fragment = (ArticleFragment) object;
        String title = fragment.getArguments().getString(ArticleFragment.ARTICLE_CATEGORY);

        int position = tabs.indexOf(title);

        if (position >= 0) {
            return position;
        } else {
            return POSITION_NONE;
        }
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.get(position);
    }
}
