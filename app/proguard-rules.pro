# --- ProGuard Rules untuk Kalkulator (Galaxy Store Readiness) ---

# --- Retrofit & OkHttp (Penting untuk Kurs) ---
-keepattributes Signature, InnerClasses, AnnotationDefault
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**

# --- Gson (Penting untuk parsing data Kurs) ---
-keep class com.google.gson.** { *; }
-keep class com.axoloth.calculator.by.sky.model.** { *; } # Sesuaikan jika Anda punya model data khusus

# --- Room Database ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# --- Google Play Services & AdMob ---
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.common.** { *; }

# --- Firebase (Analytics, Config, Crashlytics, etc) ---
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# --- Exp4j (Mesin Kalkulator) ---
-keep class net.objecthunter.exp4j.** { *; }

# --- Lottie Animation ---
-keep class com.airbnb.lottie.** { *; }

# --- Umum ---
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
