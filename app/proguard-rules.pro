# VVF Smart Manager — release ProGuard/R8 rules

# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }

# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# Hilt / Dagger generated code
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_common.** { *; }

# Kotlin coroutines / metadata
-keepattributes *Annotation*
-keepclassmembers class kotlin.Metadata { *; }

# Keep domain models (used via reflection-free Room mapping, but safe to keep names for debugging)
-keep class com.vvf.smartmanager.domain.model.** { *; }
-keep class com.vvf.smartmanager.data.local.entity.** { *; }
