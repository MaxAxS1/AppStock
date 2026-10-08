# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\maxis\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the Consumer ProGuard
# file settings in build.gradle.

# --- Stockify: Firebase / Firestore / Auth ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-keep class com.google.firestore.** { *; }
-dontwarn com.google.firebase.**
# Modelos deserializados por Firestore via reflexión: no ofuscar campos
-keep class com.appstock.app_stock.domain.model.** { *; }

# --- Navigation Compose / Lifecycle ---
-keep class androidx.navigation.** { *; }
-keep class androidx.lifecycle.** { *; }

# --- Coil ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- OkHttp / Okio ---
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
