package com.example.smartkitchen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    TextView txtWelcomeEmail;

    Button btnAIChat;
    Button btnRecipes;
    Button btnMealPlan;
    Button btnGrocery;
    Button btnNutrition;
    Button btnFavorites;
    Button btnLogout;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        // Connect XML with Java
        txtWelcomeEmail = findViewById(R.id.txtWelcomeEmail);

        btnAIChat = findViewById(R.id.btnAIChat);
        btnRecipes = findViewById(R.id.btnRecipes);
        btnMealPlan = findViewById(R.id.btnMealPlan);
        btnGrocery = findViewById(R.id.btnGrocery);
        btnNutrition = findViewById(R.id.btnNutrition);
        btnFavorites = findViewById(R.id.btnFavorites);
        btnLogout = findViewById(R.id.btnLogout);

        // Show logged-in user's email
        if (mAuth.getCurrentUser() != null) {

            String email = mAuth.getCurrentUser().getEmail();

            txtWelcomeEmail.setText(
                    "Logged in as: " + email
            );
        }

        // AI Chat button
        btnAIChat.setOnClickListener(v -> {

            // Feature will be added later
        });

        // Recipes button
        btnRecipes.setOnClickListener(v -> {

            Intent intent =
                    new Intent(HomeActivity.this, RecipeActivity.class);

            startActivity(intent);

        });

        // Meal Planner button
        btnMealPlan.setOnClickListener(v -> {

            // Meal planner will be added later
        });

        // Grocery button
        btnGrocery.setOnClickListener(v -> {

            // Grocery module will be added later
        });

        // Nutrition button
        btnNutrition.setOnClickListener(v -> {

            // Nutrition module will be added later
        });

        // Favorites button
        btnFavorites.setOnClickListener(v -> {

            // Favorites module will be added later
        });

        // Logout
        btnLogout.setOnClickListener(v -> {

            mAuth.signOut();

            Intent intent =
                    new Intent(HomeActivity.this, MainActivity.class);

            // Remove HomeActivity from back stack
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);

            finish();
        });
    }
}