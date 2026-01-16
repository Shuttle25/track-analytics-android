# Track Analytics ProGuard Rules

# Keep SimpleXML classes
-keep class org.simpleframework.** { *; }
-keepattributes *Annotation*
-keepattributes Signature

# Ignore missing javax.xml.stream classes (not used on Android)
-dontwarn javax.xml.stream.**

# Keep data models
-keep class com.drivitive.trackanalytics.data.model.** { *; }
