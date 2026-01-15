# Track Analytics ProGuard Rules

# Keep SimpleXML classes
-keep class org.simpleframework.** { *; }
-keepattributes *Annotation*
-keepattributes Signature

# Keep data models
-keep class com.shuttle25.trackanalytics.data.model.** { *; }
