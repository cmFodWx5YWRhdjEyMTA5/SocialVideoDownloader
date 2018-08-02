package com.videodownload.masterfree;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

//import com.bumptech.glide.Glide;

import com.bumptech.glide.Glide;
import com.videodownload.masterfree.network.Site;

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
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
//            imageView.setLayoutParams(new GridView.LayoutParams(4,3));
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(parent.getWidth() / 2, parent.getWidth() / 2);
//            layout.height = layout.width / 2;
            imageView.setLayoutParams(layout);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setAdjustViewBounds(true);
//            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

//        imageView.setImageResource(mThumbIds[position]);


        if (position == 1)
            imageView.setImageResource(R.drawable.facebook);
        else if (position == 0)
            imageView.setImageResource(R.drawable.vimeo);
        else if (position == 2)
            imageView.setImageResource(R.drawable.instagram);
//        else if (position == 3)
//            imageView.setImageResource(R.drawable.twitter);
        else
            Glide.with(mContext).load(sites.get(position).getImage()).into(imageView);

        return imageView;

    }
}
