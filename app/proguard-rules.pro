# 1. Optimasi Performa & Bytecode
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively

# 2. Hapus Log Debug (Mempercepat Kinerja CPU & Keamanan)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# 3. Optimasi Kotlin (Inlining)
-repackageclasses ''
-allowaccessmodification

# 4. Keamanan Dasar
-keepattributes Signature, Exceptions, *Annotation*
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# 5. Firebase & GSON (PENTING: Jangan di-obfuscate agar tidak error)
-keep class com.google.firebase.** { *; }
-keep class com.axoloth.calculator.by.sky.logic.ExchangeRateResponse { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 6. Retrofit & OkHttp
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# 7. Exp4j (Kalkulator Logic)
-keep class net.objecthunter.exp4j.** { *; }
