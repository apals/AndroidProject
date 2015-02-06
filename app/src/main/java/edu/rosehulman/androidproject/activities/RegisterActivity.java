package edu.rosehulman.androidproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import edu.rosehulman.androidproject.R;
import edu.rosehulman.androidproject.models.Drink;
import edu.rosehulman.androidproject.models.DrinkType;
import edu.rosehulman.androidproject.models.User;

/**
 * A register screen that offers registration
 */

public class RegisterActivity extends ActionBarActivity {
    private static final String USERS_CHILD = "users";
    public static final String KEY_EMAIL = "key_email";

    private Firebase mRef;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up the action bar icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.icon);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        setContentView(R.layout.activity_register);

        Firebase.setAndroidContext(this);
        this.mRef = new Firebase(getString(R.string.url));
        this.gson = new Gson();

        findViewById(R.id.email_register_button).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.email_register_button) {
                    String password = ((TextView) findViewById(R.id.register_password)).getText().toString();
                    String repeatPassword = ((TextView) findViewById(R.id.register_repeat_password)).getText().toString();

                    if (password.equals(repeatPassword)) {
                        register(
                            ((AutoCompleteTextView) findViewById(R.id.register_email)).getText().toString(),
                            ((TextView) findViewById(R.id.register_username)).getText().toString(),
                            ((TextView) findViewById(R.id.register_weight)).getText().toString(),
                            password
                        );
                    } else {
                        toast(getString(R.string.password_does_not_match_message));
                    }
                }
            }
        });
    }

    private void register(final String email, final String username, final String weight, final String password) {
        mRef.createUser(email, password, new Firebase.ResultHandler() {
                    @Override
                    public void onSuccess() {
                        createUser(email, username, weight);
                        authorize(email, password);
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        toast(firebaseError.getMessage());
                    }
                }
        );
    }

    private void authorize(final String email, final String password) {
        mRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        getUserData(email);
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        toast(firebaseError.getMessage());
                    }
                }
        );
    }

    public void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void createUser(String email, String username, String weight) {
        mRef.child(USERS_CHILD + "/" + cleanEmail(email) + "/drinkHistory").setValue("");
        mRef.child(USERS_CHILD + "/" + cleanEmail(email) + "/email").setValue(cleanEmail(email));
        mRef.child(USERS_CHILD + "/" + cleanEmail(email) + "/username").setValue(username);
        mRef.child(USERS_CHILD + "/" + cleanEmail(email) + "/weight").setValue(weight);
    }

    public String cleanEmail(String email) {
        email = email.replace(".", "-");
        email = email.replace("@", "-");
        return email;
    }
    public void getUserData(final String email) {
        mRef.child(USERS_CHILD + "/" + cleanEmail(email)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!LoginActivity.LOGGED_IN) {
                    acceptLogin(createUserFromSnapShot(dataSnapshot));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("COULDNT RETRIEVE USER DATA");
            }
        });
    }
    public User createUserFromSnapShot(DataSnapshot dataSnapshot) {
        HashMap<String, Object> userData = ((HashMap<String, Object>) dataSnapshot.getValue());

        String username = (String) userData.get("username");
        ArrayList<Drink> userDrinkList = new ArrayList<>();
        for(DataSnapshot d : dataSnapshot.getChildren()) {
            if (d.getKey().equals("drinkHistory")) {
                for(DataSnapshot child: d.getChildren()) {
                    HashMap<String, Object> drink = (HashMap<String, Object>) child.getValue();
                    double a = (double) drink.get("remainingCaffeine");
                    Date date = new Date((long) drink.get("dateTime"));

                    HashMap drinkType = (HashMap) drink.get("drinkType");
                    double caffeineAmount = (double) drinkType.get("caffeineAmount");
                    String drinkName = (String) drinkType.get("drinkName");
                    DrinkType dr = new DrinkType(drinkName, caffeineAmount);
                    Drink newDrink = new Drink(dr, date);
                    userDrinkList.add(newDrink);
                }
            }
        }
        return new User(username, userDrinkList);
    }

    public void acceptLogin(User user) {
        LoginActivity.LOGGED_IN = true;
        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
        i.putExtra(KEY_EMAIL, user);
        startActivity(i);
    }
}