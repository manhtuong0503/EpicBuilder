# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.epicbuilder.**$$serializer { *; }
-keepclassmembers class com.epicbuilder.** {
    *** Companion;
}
-keepclasseswithmembers class com.epicbuilder.** {
    kotlinx.serialization.KSerializer serializer(...);
}
