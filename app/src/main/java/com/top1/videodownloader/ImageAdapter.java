package com.top1.videodownloader;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.top1.videodownloader.network.Site;

import java.util.List;

/**
 * Created by muicv on 6/18/2017.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private List<Site> sites;

    public ImageAdapter(Context c, List<Site> listSite) {
        this.mContext = c;
        this.sites = listSite;
    }

    @Override
    public int getCount() {
        if (sites == null)
            return 0;
        return sites.size();
    }

    @Override
    public Object getItem(int position) {
        return sites.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;//sites.get(position).hashCode();//position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
//            imageView.setLayoutParams(new GridView.LayoutParams(4,3));
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(parent.getWidth() / 2, parent.getWidth() / 3);
            imageView.setLayoutParams(layout);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//            imageView.setAdjustViewBounds(true);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(parent.getWidth() / 2, parent.getWidth() / 3);
            imageView.setLayoutParams(layout);
        }

//        imageView.setLayoutParams(new
//                AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
//                AbsListView.LayoutParams.MATCH_PARENT));
//        imageView.setImageResource(mThumbIds[position]);


        if (sites.get(position).getUrl().contains("facebook"))
            imageView.setImageResource(R.drawable.facebook);
        else if (sites.get(position).getUrl().contains("vimeo"))
            imageView.setImageResource(R.drawable.vimeo);
        else if (sites.get(position).getUrl().contains("twitter"))
            imageView.setImageResource(R.drawable.twitter);
        else if (sites.get(position).getUrl().contains("instagram"))
            imageView.setImageResource(R.drawable.instagram);
        else {
            Glide.with(mContext).load(sites.get(position).getImage()).into(imageView);
        }

        return imageView;

    }
}