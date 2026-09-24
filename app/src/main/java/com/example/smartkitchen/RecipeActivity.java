package com.example.smartkitchen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class RecipeActivity extends AppCompatActivity {

    LinearLayout cardFeatured;
    LinearLayout cardPasta;
    LinearLayout cardBiryani;
    LinearLayout cardPancakes;
    LinearLayout cardSalad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recipe);

        cardFeatured = findViewById(R.id.cardFeatured);
        cardPasta = findViewById(R.id.cardPasta);
        cardBiryani = findViewById(R.id.cardBiryani);
        cardPancakes = findViewById(R.id.cardPancakes);
        cardSalad = findViewById(R.id.cardSalad);


        // Featured Pasta
        cardFeatured.setOnClickListener(v -> {

            openRecipe("pasta");

        });


        // Pasta
        cardPasta.setOnClickListener(v -> {

            openRecipe("pasta");

        });


        // Biryani
        cardBiryani.setOnClickListener(v -> {

            openRecipe("biryani");

        });


        // Pancakes
        cardPancakes.setOnClickListener(v -> {

            openRecipe("pancakes");

        });


        // Salad
        cardSalad.setOnClickListener(v -> {

            openRecipe("salad");

        });

    }


    private void openRecipe(String recipeId) {

        Intent intent =
                new Intent(
                        RecipeActivity.this,
                        RecipeDetailActivity.class
                );

        intent.putExtra("recipe_id", recipeId);

        startActivity(intent);

    }

}