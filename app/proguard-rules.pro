# kotlinx.serialization: keep generated serializers for @Serializable classes
# (asset models and type-safe navigation routes).
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers @kotlinx.serialization.Serializable class com.simkeeper.app.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.simkeeper.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class com.simkeeper.app.**
-keepclassmembers class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room: entities and DAOs are referenced by generated code; the Room library ships
# consumer rules, so nothing extra is needed here beyond keeping our entity names stable.
-keep class com.simkeeper.app.data.db.** { *; }
