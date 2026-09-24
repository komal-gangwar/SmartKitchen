package com.example.smartkitchen;

public class Recipe {

    String id;
    String name;
    String emoji;
    String rating;
    String time;
    String difficulty;
    String description;
    String nutrition;
    String ingredients;

    String step1Title;
    String step1Description;

    String step2Title;
    String step2Description;

    String step3Title;
    String step3Description;

    String step4Title;
    String step4Description;

    String step5Title;
    String step5Description;

    String chefTips;


    public Recipe(
            String id,
            String name,
            String emoji,
            String rating,
            String time,
            String difficulty,
            String description,
            String nutrition,
            String ingredients,

            String step1Title,
            String step1Description,

            String step2Title,
            String step2Description,

            String step3Title,
            String step3Description,

            String step4Title,
            String step4Description,

            String step5Title,
            String step5Description,

            String chefTips
    ) {

        this.id = id;
        this.name = name;
        this.emoji = emoji;
        this.rating = rating;
        this.time = time;
        this.difficulty = difficulty;
        this.description = description;
        this.nutrition = nutrition;
        this.ingredients = ingredients;

        this.step1Title = step1Title;
        this.step1Description = step1Description;

        this.step2Title = step2Title;
        this.step2Description = step2Description;

        this.step3Title = step3Title;
        this.step3Description = step3Description;

        this.step4Title = step4Title;
        this.step4Description = step4Description;

        this.step5Title = step5Title;
        this.step5Description = step5Description;

        this.chefTips = chefTips;
    }
}