package com.example.smartkitchen;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RecipeDetailActivity extends AppCompatActivity {

    TextView txtBack;

    TextView txtRecipeEmoji;
    TextView txtRecipeName;

    TextView txtRating;
    TextView txtTime;
    TextView txtDifficulty;

    TextView txtDescription;
    TextView txtServings;
    TextView txtNutrition;
    TextView txtIngredients;

    TextView txtStep1Title;
    TextView txtStep1Description;

    TextView txtStep2Title;
    TextView txtStep2Description;

    TextView txtStep3Title;
    TextView txtStep3Description;

    TextView txtStep4Title;
    TextView txtStep4Description;

    TextView txtStep5Title;
    TextView txtStep5Description;

    Button btnMinus;
    Button btnPlus;
    Button btnFavorite;
    Button btnStartCooking;

    int servings = 2;

    Recipe currentRecipe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recipe_detail);


        // -----------------------------------------
        // CONNECT XML VIEWS
        // -----------------------------------------

        txtBack = findViewById(R.id.txtBack);

        txtRecipeEmoji = findViewById(R.id.txtRecipeEmoji);
        txtRecipeName = findViewById(R.id.txtRecipeName);

        txtRating = findViewById(R.id.txtRating);
        txtTime = findViewById(R.id.txtTime);
        txtDifficulty = findViewById(R.id.txtDifficulty);

        txtDescription = findViewById(R.id.txtDescription);
        txtServings = findViewById(R.id.txtServings);
        txtNutrition = findViewById(R.id.txtNutrition);
        txtIngredients = findViewById(R.id.txtIngredients);


        txtStep1Title = findViewById(R.id.txtStep1Title);
        txtStep1Description = findViewById(R.id.txtStep1Description);

        txtStep2Title = findViewById(R.id.txtStep2Title);
        txtStep2Description = findViewById(R.id.txtStep2Description);

        txtStep3Title = findViewById(R.id.txtStep3Title);
        txtStep3Description = findViewById(R.id.txtStep3Description);

        txtStep4Title = findViewById(R.id.txtStep4Title);
        txtStep4Description = findViewById(R.id.txtStep4Description);

        txtStep5Title = findViewById(R.id.txtStep5Title);
        txtStep5Description = findViewById(R.id.txtStep5Description);


        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);

        btnFavorite = findViewById(R.id.btnFavorite);
        btnStartCooking = findViewById(R.id.btnStartCooking);


        // -----------------------------------------
        // GET SELECTED RECIPE ID
        // -----------------------------------------

        String recipeId =
                getIntent().getStringExtra("recipe_id");


        // -----------------------------------------
        // FIND RECIPE
        // -----------------------------------------

        currentRecipe =
                RecipeData.getRecipeById(recipeId);


        // -----------------------------------------
        // DISPLAY RECIPE
        // -----------------------------------------

        if (currentRecipe != null) {

            displayRecipe();

        }


        // -----------------------------------------
        // BACK
        // -----------------------------------------

        txtBack.setOnClickListener(v -> {

            finish();

        });


        // -----------------------------------------
        // MINUS SERVINGS
        // -----------------------------------------

        btnMinus.setOnClickListener(v -> {

            if (servings > 1) {

                servings--;

                updateServings();

            }

        });


        // -----------------------------------------
        // PLUS SERVINGS
        // -----------------------------------------

        btnPlus.setOnClickListener(v -> {

            if (servings < 10) {

                servings++;

                updateServings();

            }

        });


        // -----------------------------------------
        // FAVORITE
        // -----------------------------------------

        btnFavorite.setOnClickListener(v -> {

            btnFavorite.setText(
                    "♥  ADDED TO FAVORITES"
            );

            Toast.makeText(
                    RecipeDetailActivity.this,
                    "Recipe saved to favorites ❤️",
                    Toast.LENGTH_SHORT
            ).show();

        });


        // -----------------------------------------
        // START COOKING
        // -----------------------------------------

        btnStartCooking.setOnClickListener(v -> {

            Toast.makeText(
                    RecipeDetailActivity.this,
                    "Cooking Mode coming next 👩‍🍳",
                    Toast.LENGTH_SHORT
            ).show();

        });


        updateServings();
    }


    // =================================================
    // DISPLAY RECIPE DATA
    // =================================================

    private void displayRecipe() {

        txtRecipeEmoji.setText(
                currentRecipe.emoji
        );

        txtRecipeName.setText(
                currentRecipe.name
        );

        txtRating.setText(
                "⭐ " + currentRecipe.rating
        );

        txtTime.setText(
                "⏱ " + currentRecipe.time
        );

        txtDifficulty.setText(
                currentRecipe.difficulty
        );

        txtDescription.setText(
                currentRecipe.description
        );

        txtNutrition.setText(
                currentRecipe.nutrition
        );

        txtIngredients.setText(
                currentRecipe.ingredients
        );


        // Step 1
        txtStep1Title.setText(
                "01  •  " + currentRecipe.step1Title
        );

        txtStep1Description.setText(
                currentRecipe.step1Description
        );


        // Step 2
        txtStep2Title.setText(
                "02  •  " + currentRecipe.step2Title
        );

        txtStep2Description.setText(
                currentRecipe.step2Description
        );


        // Step 3
        txtStep3Title.setText(
                "03  •  " + currentRecipe.step3Title
        );

        txtStep3Description.setText(
                currentRecipe.step3Description
        );


        // Step 4
        txtStep4Title.setText(
                "04  •  " + currentRecipe.step4Title
        );

        txtStep4Description.setText(
                currentRecipe.step4Description
        );


        // Step 5
        txtStep5Title.setText(
                "05  •  " + currentRecipe.step5Title
        );

        txtStep5Description.setText(
                currentRecipe.step5Description
        );

    }


    // =================================================
    // SERVINGS
    // =================================================

    private void updateServings() {

        txtServings.setText(
                servings + " servings"
        );

    }

}