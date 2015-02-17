package edu.rosehulman.androidproject.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by palssoa on 1/25/2015.
 */

public class User implements Comparable, Serializable {
    private static final long KEEP_HISTORY = 48; //Hours to keep drink history

    private ArrayList<Drink> mDrinkHistory;
    private String mUsername;
    private ArrayList<DateCaffeinePoint> points = new ArrayList<>();
    private int mWeight;
    private String mGender;
    private String mEmail;

    public User(String username, String email, int weight, String gender, ArrayList<Drink> drinkHistory) {
        mEmail = email;
        mWeight = weight;
        mGender = gender;
        mUsername = username;
        mDrinkHistory = drinkHistory;
    }

    public String getUsername() {
        return mUsername;
    }


    public double getCaffeineLevel(Date date) {
        double caffeineLevel = 0;

        for (int i = 0; i < mDrinkHistory.size(); i++) {
            double caffeine = mDrinkHistory.get(i).getRemainingCaffeine(date, mWeight, mGender);
            if(mDrinkHistory.get(i).getSecondsPassed() < 3600*KEEP_HISTORY)
                caffeineLevel += caffeine;
            else
                mDrinkHistory.remove(mDrinkHistory.get(i));
        }

        //No user should have a caffeine level over 30
        caffeineLevel = (caffeineLevel > 30) ? 30 : caffeineLevel;
        return caffeineLevel;
    }

    public double getCaffeineLevel() {
        return getCaffeineLevel(new Date());
    }

    public void drink(Drink drink) {
        mDrinkHistory.add(drink);
    }
    public ArrayList<Drink> getDrinkHistory() {
        return mDrinkHistory;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", mUsername);
        userMap.put("drinkHistory", mDrinkHistory);
        return userMap;

    }

    @Override
    public int compareTo(Object another) {
        double compare = ((User) another).getCaffeineLevel() - getCaffeineLevel();
        return (int)Math.round(compare);
    }


    public void setDrinkHistory(ArrayList<Drink> drinkHistory) {
        this.mDrinkHistory = drinkHistory;
    }

    public void addPoint(Date date, double caffeine) {
        points.add(new DateCaffeinePoint(date, caffeine));
    }

    public void removePoint(int i) {
        points.remove(i);
    }

    public ArrayList<DateCaffeinePoint> getPoints() {
        return points;
    }

    public String getEmail() {
        return mEmail;
    }

    public boolean clearOldDrinks() {
        boolean someThingRemoved = false;
        for (int i = 0; i < mDrinkHistory.size(); i++) {
            if (mDrinkHistory.get(i).getSecondsPassed() > 3600*KEEP_HISTORY) {
                mDrinkHistory.remove(i);
                someThingRemoved = true;
            }
        }
        return someThingRemoved;
    }

    public int getWeight() {
        return mWeight;
    }

    public String getGender() {
        return mGender;
    }
}
