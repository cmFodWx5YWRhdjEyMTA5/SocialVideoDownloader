package com.downloadapp.videodownloadmaster;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeTabFragment} interface
 * to handle interaction events.
 * Use the {@link HomeTabFragment} factory method to
 * create an instance of this fragment.
 */
public class HomeTabFragment extends Fragment {

    private GridView gridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        gridView = (GridView) view.findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Main2Activity main =(Main2Activity) getActivity();
                main.loadUrlWebview(Main2Activity.jsonConfig.getUrlAccept().get(position));
            }

        });

        loadDataGridView();
    }

    public  void loadDataGridView()
    {
        if (Main2Activity.jsonConfig != null)
        {
            ImageAdapter adapter = new ImageAdapter(getActivity(), Main2Activity.jsonConfig.getUrlAccept());
            gridView.setAdapter(adapter);
        }
    }



}
