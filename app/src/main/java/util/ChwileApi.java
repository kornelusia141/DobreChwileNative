package util;

import android.app.Application;

public class ChwileApi extends Application {
    private String userId;
    private static ChwileApi instance;

    public static ChwileApi getInstance(){
        if(instance == null)
            instance = new ChwileApi();
        return instance;

    }
    public ChwileApi(){}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
