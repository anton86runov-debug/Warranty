plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ktlint) apply false
}

allprojects {
    tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask> {
        reportsOutputDirectory.set(layout.buildDirectory.dir("reports/ktlint"))
    }
}

tasks.register("ci") {
    group = "verification"
    description = "Runs lint, unit tests, and assembles debug build."
    dependsOn(
        ":app:ktlintCheck",
        ":app:testDebugUnitTest",
        ":app:assembleDebug"
    )
}
