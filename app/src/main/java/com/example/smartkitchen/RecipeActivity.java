package com.example.smartkitchen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RecipeActivity extends AppCompatActivity {

    LinearLayout cardFeatured;
    LinearLayout cardPasta;
    LinearLayout cardBiryani;
    LinearLayout cardPancakes;
    LinearLayout cardSalad;
    EditText edtSearchRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recipe);

        cardFeatured = findViewById(R.id.cardFeatured);
        cardPasta = findViewById(R.id.cardPasta);
        cardBiryani = findViewById(R.id.cardBiryani);
        cardPancakes = findViewById(R.id.cardPancakes);
        cardSalad = findViewById(R.id.cardSalad);
        edtSearchRecipe = findViewById(R.id.edtSearchRecipe);

        edtSearchRecipe.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {
                String request = edtSearchRecipe.getText().toString().trim();
                if (request.isEmpty()) return true;
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(edtSearchRecipe.getWindowToken(), 0);
                edtSearchRecipe.setEnabled(false);
                Toast.makeText(this, "Checking saved recipes and creating yours if needed…", Toast.LENGTH_SHORT).show();
                RecipeRepository.findOrGenerate(request, new RecipeRepository.Callback() {
                    @Override public void onResult(Recipe recipe) {
                        edtSearchRecipe.setEnabled(true);
                        openRecipe(recipe.id);
                    }
                    @Override public void onError(Exception error) {
                        edtSearchRecipe.setEnabled(true);
                        Toast.makeText(RecipeActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                return true;
            }
            return false;
        });


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
