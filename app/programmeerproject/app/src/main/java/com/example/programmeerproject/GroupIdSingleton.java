package com.example.programmeerproject;

/**
 * Plnnr
 * Sidney de Vries (10724087)
 *
 * Singleton used to keep groupId consistent when using PinoardTabActivity tabs
 *
 * Source: https://gist.github.com/Akayh/5566992
 */

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
