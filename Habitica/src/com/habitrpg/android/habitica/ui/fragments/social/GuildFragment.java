package com.habitrpg.android.habitica.ui.fragments.social;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.ContentCache;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.UserParty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GuildFragment extends BaseMainFragment implements Callback<Group> {

    private Group guild;
    public boolean isMember;

    public ViewPager viewPager;

    private GroupInformationFragment guildInformationFragment;
    private ChatListFragment chatListFragment;

    public void setGuild(Group guild) {
        this.guild = guild;
        if (this.guildInformationFragment != null) {
            this.guildInformationFragment.setGroup(guild);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.usesTabLayout = true;
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_party, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.view_pager);

        viewPager.setCurrentItem(0);

        final ContentCache contentCache = new ContentCache(mAPIHelper.apiService);

        // Get the full group data
        mAPIHelper.apiService.getGroup(this.guild.id, new Callback<Group>() {
            @Override
            public void success(Group group, Response response) {
                if (group == null) {
                    tabLayout.removeAllTabs();
                    return;
                }
                GuildFragment.this.guild = group;

                if (guildInformationFragment != null) {
                    guildInformationFragment.setGroup(group);
                }

                if (chatListFragment != null) {
                    chatListFragment.seenGroupId = group.id;
                }
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });

        setViewPagerAdapter();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.isMember) {
            if (this.user.getId().equals(this.guild.leaderID)) {
                this.activity.getMenuInflater().inflate(R.menu.guild_admin, menu);
            } else {
                this.activity.getMenuInflater().inflate(R.menu.guild_member, menu);
            }
        } else {
            this.activity.getMenuInflater().inflate(R.menu.guild_nonmember, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_guild_join:
                this.mAPIHelper.apiService.joinGroup(this.guild.id, this);
                return true;
            case R.id.menu_guild_leave:
                this.mAPIHelper.apiService.leaveGroup(this.guild.id, this);
                return true;
            case R.id.menu_guild_edit:
                this.displayEditForm();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setViewPagerAdapter() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

        UserParty party = user.getParty();

        if (party == null) {
            return;
        }

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {

                Fragment fragment;

                switch (position) {
                    case 0: {
                        fragment = guildInformationFragment = GroupInformationFragment.newInstance(GuildFragment.this.guild);
                        break;
                    }
                    case 1: {
                        chatListFragment = new ChatListFragment();
                        chatListFragment.configure(activity, GuildFragment.this.guild.id, mAPIHelper, user, activity, false);
                        fragment = chatListFragment;
                        break;
                    }
                    default:
                        fragment = new Fragment();
                }

                return fragment;
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return activity.getString(R.string.guild);
                    case 1:
                        return activity.getString(R.string.chat);
                }
                return "";
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1 && GuildFragment.this.guild != null) {
                    chatListFragment.setNavigatedToFragment(GuildFragment.this.guild.id);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1 && GuildFragment.this.guild != null) {
                    chatListFragment.setNavigatedToFragment(GuildFragment.this.guild.id);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void success(Group group, Response response) {
        this.guild = group;
        this.guildInformationFragment.setGroup(group);
    }

    @Override
    public void failure(RetrofitError error) {

    }

    private void displayEditForm() {
        Bundle bundle = new Bundle();
        bundle.putString("groupID",this.guild.id);

        Intent intent = new Intent(activity, TaskFormActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}