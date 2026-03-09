# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Hilt
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* <fields>;
    @javax.inject.* <methods>;

}
