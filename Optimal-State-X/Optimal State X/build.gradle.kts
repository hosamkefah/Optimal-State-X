plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // Add this line
    alias(libs.plugins.google.gms.google.services) apply false
    //alias(libs.plugins.google.gms.google.services) apply false
}