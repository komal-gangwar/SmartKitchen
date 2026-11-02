# Dynamic recipe setup

Recipe searches use the `recipes` collection in Cloud Firestore. On a cache miss, the app asks Gemini for a structured recipe, saves it under a stable ID derived from the normalized search, and opens that saved recipe. Existing sample cards still open their bundled recipes unless a matching Firestore document exists.

Before running the feature:

1. Enable Cloud Firestore for the Firebase project associated with `google-services.json` and configure authenticated read/write rules for the `recipes` collection.
2. Set `GEMINI_API_KEY` in the Gradle user properties file (`%USERPROFILE%\.gradle\gradle.properties`) or as an environment variable, then rebuild. Do not commit the key or put it in source control.

**Key security:** this Android implementation calls Gemini directly, so Gradle embeds the key in the APK. This is suitable only for a private prototype with a restricted, quota-limited key. For a public release, move Gemini generation behind an authenticated server endpoint and keep the key in server-side secrets.
