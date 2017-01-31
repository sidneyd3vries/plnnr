package com.example.programmeerproject;

public class GroupIdSingleton {

    private static GroupIdSingleton mInstance = null;

    private String groupId;

    private GroupIdSingleton(){
        groupId = "";
    }

    public static GroupIdSingleton getInstance(){
        if(mInstance == null)
        {
            mInstance = new GroupIdSingleton();
        }
        return mInstance;
    }

    public String getString(){
        return this.groupId;
    }

    public void setString(String value){
        groupId = value;
    }
}
