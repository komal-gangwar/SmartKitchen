package com.example.smartkitchen;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Locale;

public class CookingModeActivity extends AppCompatActivity {

    TextView btnCookingBack;
    TextView txtCookingRecipeName;
    TextView txtStepNumber;
    TextView txtProgressPercent;
    ProgressBar cookingProgress;
    TextView txtCookingStepTitle;
    TextView txtCookingStepDescription;
    TextView txtCookingTip;
    TextView txtAudioStatus;
    TextView txtAudioExpiry;
    SeekBar audioSeekBar;
    TextView txtAudioCurrentTime;
    TextView txtAudioTotalTime;
    TextView txtSpeedInfo;

    Button btnRestartAudio;
    Button btnPlayPauseAudio;
    Button btnPlaybackSpeed;
    Button btnNextStep;
    TextView txtExitCooking;

    Recipe currentRecipe;

    int currentStep = 1;
    final int totalSteps = 5;

    TextToSpeech textToSpeech;
    MediaPlayer mediaPlayer;
    File audioFile;

    boolean audioReady = false;
    boolean isGeneratingAudio = false;

    float playbackSpeed = 1.0f;

    final float[] playbackSpeeds = {
            0.75f, 1.0f, 1.25f, 1.5f, 2.0f
    };

    int speedIndex = 1;

    final android.os.Handler progressHandler =
            new android.os.Handler(android.os.Looper.getMainLooper());

    final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            updateAudioProgress();

            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                progressHandler.postDelayed(this, 500);
            }
        }
    };

    private static final long AUDIO_LIFETIME =
            24L * 60L * 60L * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cooking_mode);

        btnCookingBack = findViewById(R.id.btnCookingBack);
        txtCookingRecipeName = findViewById(R.id.txtCookingRecipeName);
        txtStepNumber = findViewById(R.id.txtStepNumber);
        txtProgressPercent = findViewById(R.id.txtProgressPercent);
        cookingProgress = findViewById(R.id.cookingProgress);
        txtCookingStepTitle = findViewById(R.id.txtCookingStepTitle);
        txtCookingStepDescription = findViewById(R.id.txtCookingStepDescription);
        txtCookingTip = findViewById(R.id.txtCookingTip);
        txtAudioStatus = findViewById(R.id.txtAudioStatus);
        txtAudioExpiry = findViewById(R.id.txtAudioExpiry);
        audioSeekBar = findViewById(R.id.audioSeekBar);
        txtAudioCurrentTime = findViewById(R.id.txtAudioCurrentTime);
        txtAudioTotalTime = findViewById(R.id.txtAudioTotalTime);
        txtSpeedInfo = findViewById(R.id.txtSpeedInfo);

        btnRestartAudio = findViewById(R.id.btnRestartAudio);
        btnPlayPauseAudio = findViewById(R.id.btnPlayPauseAudio);
        btnPlaybackSpeed = findViewById(R.id.btnPlaybackSpeed);
        btnNextStep = findViewById(R.id.btnNextStep);
        txtExitCooking = findViewById(R.id.txtExitCooking);

        String recipeId = getIntent().getStringExtra("recipe_id");
        btnNextStep.setEnabled(false);
        RecipeRepository.getById(recipeId, new RecipeRepository.Callback() {
            @Override public void onResult(Recipe recipe) {
                currentRecipe = recipe;
                txtCookingRecipeName.setText(currentRecipe.name);
                showCurrentStep();
                btnNextStep.setEnabled(true);
                prepareCookingAudio(currentRecipe);
            }

            @Override public void onError(Exception error) {
                Toast.makeText(CookingModeActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });

        btnPlayPauseAudio.setEnabled(false);
        btnRestartAudio.setEnabled(false);
        btnPlaybackSpeed.setEnabled(false);
        audioSeekBar.setEnabled(false);

        txtAudioStatus.setText("Preparing your cooking audio... 🎧");
        txtAudioExpiry.setText("Creating your 24-hour cooking guide...");

        btnCookingBack.setOnClickListener(v -> {
            releaseAudio();
            finish();
        });

        btnPlayPauseAudio.setOnClickListener(v -> togglePlayPause());

        btnRestartAudio.setOnClickListener(v -> restartAudio());

        btnPlaybackSpeed.setOnClickListener(v -> changePlaybackSpeed());

        audioSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {
                        if (fromUser && mediaPlayer != null && audioReady) {
                            mediaPlayer.seekTo(progress);
                            updateAudioTime(
                                    progress,
                                    mediaPlayer.getDuration()
                            );
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );

        btnNextStep.setOnClickListener(v -> goToNextStep());

        txtExitCooking.setOnClickListener(v -> {
            releaseAudio();
            finish();
        });
    }

    private void prepareCookingAudio(Recipe recipe) {
        cleanupOldAudioFiles();

        File audioDirectory =
                new File(getFilesDir(), "cooking_audio");

        if (!audioDirectory.exists()) {
            audioDirectory.mkdirs();
        }

        audioFile = new File(
                audioDirectory,
                recipe.id + "_cooking.wav"
        );

        if (audioFile.exists()) {
            long fileAge =
                    System.currentTimeMillis() - audioFile.lastModified();

            if (fileAge < AUDIO_LIFETIME) {
                initializeMediaPlayer();
                return;
            } else {
                audioFile.delete();
            }
        }

        generateCookingAudio(recipe);
    }

    private void generateCookingAudio(Recipe recipe) {
        if (isGeneratingAudio) {
            return;
        }

        isGeneratingAudio = true;

        txtAudioStatus.setText(
                "Creating your cooking guide... 🎧"
        );

        String audioText =
                "Welcome to Smart Kitchen. Let's cook "
                        + recipe.name + ". "

                        + "Step 1. "
                        + recipe.step1Title + ". "
                        + recipe.step1Description + ". "

                        + "Step 2. "
                        + recipe.step2Title + ". "
                        + recipe.step2Description + ". "

                        + "Step 3. "
                        + recipe.step3Title + ". "
                        + recipe.step3Description + ". "

                        + "Step 4. "
                        + recipe.step4Title + ". "
                        + recipe.step4Description + ". "

                        + "Step 5. "
                        + recipe.step5Title + ". "
                        + recipe.step5Description + ". "

                        + "You have completed the cooking instructions. "
                        + "Great job, Chef!";

        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.SUCCESS) {
                isGeneratingAudio = false;
                showAudioError();
                return;
            }

            int languageResult =
                    textToSpeech.setLanguage(Locale.US);

            if (languageResult == TextToSpeech.LANG_MISSING_DATA
                    || languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                isGeneratingAudio = false;
                showAudioError();
                return;
            }

            textToSpeech.setSpeechRate(0.95f);

            textToSpeech.setOnUtteranceProgressListener(
                    new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            runOnUiThread(() ->
                                    txtAudioStatus.setText(
                                            "Generating cooking audio... 🎙️"
                                    )
                            );
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            runOnUiThread(() -> {
                                isGeneratingAudio = false;

                                if (textToSpeech != null) {
                                    textToSpeech.stop();
                                    textToSpeech.shutdown();
                                    textToSpeech = null;
                                }

                                initializeMediaPlayer();
                            });
                        }

                        @Override
                        public void onError(String utteranceId) {
                            runOnUiThread(() -> {
                                isGeneratingAudio = false;
                                showAudioError();
                            });
                        }
                    }
            );

            android.os.Bundle params = new android.os.Bundle();

            int result = textToSpeech.synthesizeToFile(
                    audioText,
                    params,
                    audioFile,
                    "SmartKitchenCookingAudio"
            );

            if (result == TextToSpeech.ERROR) {
                runOnUiThread(() -> {
                    isGeneratingAudio = false;
                    showAudioError();
                });
            }
        });
    }

    private void initializeMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            mediaPlayer = new MediaPlayer();

            mediaPlayer.setDataSource(audioFile.getAbsolutePath());

            mediaPlayer.setOnPreparedListener(mp -> {
                audioReady = true;

                int duration = mp.getDuration();

                audioSeekBar.setMax(duration);
                audioSeekBar.setProgress(0);

                txtAudioCurrentTime.setText(formatTime(0));
                txtAudioTotalTime.setText(formatTime(duration));

                txtAudioStatus.setText(
                        "Your cooking guide is ready! 💕"
                );

                txtAudioExpiry.setText(
                        "Audio available for 24 hours"
                );

                btnPlayPauseAudio.setEnabled(true);
                btnRestartAudio.setEnabled(true);
                btnPlaybackSpeed.setEnabled(true);
                audioSeekBar.setEnabled(true);

                btnPlayPauseAudio.setText("▶");
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                mp.seekTo(0);

                audioSeekBar.setProgress(0);
                txtAudioCurrentTime.setText(formatTime(0));
                btnPlayPauseAudio.setText("▶");

                progressHandler.removeCallbacks(progressRunnable);
            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            showAudioError();
        }
    }

    private void togglePlayPause() {
        if (!audioReady || mediaPlayer == null) {
            return;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPauseAudio.setText("▶");
            progressHandler.removeCallbacks(progressRunnable);
        } else {
            mediaPlayer.start();
            btnPlayPauseAudio.setText("⏸");
            progressHandler.post(progressRunnable);
        }
    }

    private void restartAudio() {
        if (!audioReady || mediaPlayer == null) {
            return;
        }

        mediaPlayer.seekTo(0);

        txtAudioCurrentTime.setText(formatTime(0));
        audioSeekBar.setProgress(0);

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }

        btnPlayPauseAudio.setText("⏸");
        progressHandler.post(progressRunnable);
    }

    private void changePlaybackSpeed() {
        if (!audioReady || mediaPlayer == null) {
            return;
        }

        speedIndex++;

        if (speedIndex >= playbackSpeeds.length) {
            speedIndex = 0;
        }

        playbackSpeed = playbackSpeeds[speedIndex];

        try {
            mediaPlayer.setPlaybackParams(
                    mediaPlayer.getPlaybackParams()
                            .setSpeed(playbackSpeed)
            );

            String speedText = String.format(
                    Locale.US,
                    "%.2f×",
                    playbackSpeed
            );

            btnPlaybackSpeed.setText(speedText);
            txtSpeedInfo.setText(
                    "Playback speed: " + speedText
            );

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Playback speed is not supported.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void updateAudioProgress() {
        if (mediaPlayer == null || !audioReady) {
            return;
        }

        try {
            int currentPosition =
                    mediaPlayer.getCurrentPosition();

            int duration =
                    mediaPlayer.getDuration();

            audioSeekBar.setProgress(currentPosition);

            updateAudioTime(
                    currentPosition,
                    duration
            );

        } catch (Exception ignored) {
        }
    }

    private void updateAudioTime(int current, int total) {
        txtAudioCurrentTime.setText(formatTime(current));
        txtAudioTotalTime.setText(formatTime(total));
    }

    private String formatTime(int milliseconds) {
        int totalSeconds = milliseconds / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format(
                Locale.US,
                "%d:%02d",
                minutes,
                seconds
        );
    }

    private void showAudioError() {
        txtAudioStatus.setText(
                "Unable to create the cooking audio."
        );

        txtAudioExpiry.setText(
                "You can still follow the written instructions."
        );

        btnPlayPauseAudio.setEnabled(false);
        btnRestartAudio.setEnabled(false);
        btnPlaybackSpeed.setEnabled(false);
        audioSeekBar.setEnabled(false);

        Toast.makeText(
                this,
                "Audio generation failed. Please try again.",
                Toast.LENGTH_LONG
        ).show();
    }

    private void showCurrentStep() {
        if (currentRecipe == null) return;
        txtCookingRecipeName.setText(currentRecipe.name);

        txtStepNumber.setText(
                "Step " + currentStep + " of " + totalSteps
        );

        int percentage =
                (currentStep * 100) / totalSteps;

        txtProgressPercent.setText(percentage + "%");
        cookingProgress.setProgress(currentStep);

        String title = "";
        String description = "";

        if (currentStep == 1) {
            title = currentRecipe.step1Title;
            description = currentRecipe.step1Description;
        } else if (currentStep == 2) {
            title = currentRecipe.step2Title;
            description = currentRecipe.step2Description;
        } else if (currentStep == 3) {
            title = currentRecipe.step3Title;
            description = currentRecipe.step3Description;
        } else if (currentStep == 4) {
            title = currentRecipe.step4Title;
            description = currentRecipe.step4Description;
        } else if (currentStep == 5) {
            title = currentRecipe.step5Title;
            description = currentRecipe.step5Description;
        }

        txtCookingStepTitle.setText(title);
        txtCookingStepDescription.setText(description);

        if (currentStep == 1) {
            txtCookingTip.setText(
                    "Read the complete step before starting. "
                            + "Keep all required ingredients nearby."
            );
        } else if (currentStep == 2) {
            txtCookingTip.setText(
                    "Keep your heat under control and watch "
                            + "the food instead of relying only on time."
            );
        } else if (currentStep == 3) {
            txtCookingTip.setText(
                    "Add ingredients gradually and keep stirring "
                            + "when the recipe requires it."
            );
        } else if (currentStep == 4) {
            txtCookingTip.setText(
                    "Check the texture and consistency before "
                            + "moving to the next step."
            );
        } else {
            txtCookingTip.setText(
                    "Taste carefully, make final adjustments, "
                            + "and serve while the food is at its best."
            );
        }

        if (currentStep == totalSteps) {
            btnNextStep.setText("🎉  FINISH COOKING");
        } else {
            btnNextStep.setText("✓  I'M DONE — NEXT STEP");
        }
    }

    private void goToNextStep() {
        if (currentStep < totalSteps) {
            currentStep++;
            showCurrentStep();
        } else {
            showCookingComplete();
        }
    }

    private void showCookingComplete() {
        txtStepNumber.setText("🎉 Cooking Complete!");
        txtProgressPercent.setText("100%");
        cookingProgress.setProgress(totalSteps);

        txtCookingStepTitle.setText(
                "Your " + currentRecipe.name + " is ready! 🍽️"
        );

        txtCookingStepDescription.setText(
                "Great job! Take a moment to plate your dish "
                        + "and enjoy your freshly prepared meal."
        );

        txtCookingTip.setText(
                "Chef's tip: Taste your final dish before serving "
                        + "and adjust seasoning if needed."
        );

        btnNextStep.setText("✓  DONE");

        btnNextStep.setOnClickListener(v -> {
            releaseAudio();
            finish();
        });
    }

    private void cleanupOldAudioFiles() {
        File audioDirectory =
                new File(getFilesDir(), "cooking_audio");

        if (!audioDirectory.exists()) {
            return;
        }

        File[] files = audioDirectory.listFiles();

        if (files == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }

            long age =
                    currentTime - file.lastModified();

            if (age >= AUDIO_LIFETIME) {
                file.delete();
            }
        }
    }

    private void releaseAudio() {
        progressHandler.removeCallbacks(progressRunnable);

        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (Exception ignored) {
            }

            mediaPlayer.release();
            mediaPlayer = null;
        }

        audioReady = false;

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    @Override
    protected void onDestroy() {
        releaseAudio();
        super.onDestroy();
    }
}
