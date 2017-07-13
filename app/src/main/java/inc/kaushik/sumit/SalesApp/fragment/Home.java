package inc.kaushik.sumit.SalesApp.fragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import inc.kaushik.sumit.SalesApp.R;
import inc.kaushik.sumit.SalesApp.ShowDataInListView;
import inc.kaushik.sumit.SalesApp.ShowDataInMapView;

public class Home extends Fragment {
private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =inflater.inflate(R.layout.fragment_home, container, false);
        mSectionsPagerAdapter=new SectionsPagerAdapter(getChildFragmentManager());
        mViewPager=(ViewPager)view.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout=(TabLayout)view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter{
        public SectionsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new ShowDataInListView();
                case 1:
                    return new ShowDataInMapView();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "ListView";
                case 1:
                    return "Map View";
            }
            return null;
        }
    }

}
