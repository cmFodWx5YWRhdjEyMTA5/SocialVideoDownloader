package com.top1.videodownloader.network;

/**
 * Created by Muicv on 3/27/2018.
 */

public class Suggest {
    private  String name;
    private  String [] listQuery;

    public String getName(){return  name;}
    public String[] getListQuery(){return  listQuery;}

    public  void setName (String name ) {
        this.name = name;
    }

    public  void setListQuery (String[] listQuery ) {
        this.listQuery = listQuery;
    }
}
