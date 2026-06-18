import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    checkstyle
    signing
    id("com.diffplug.spotless") version "7.0.4"
    id("com.gradleup.shadow") version "9.0.2"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = findProperty("group") as String
version = findProperty("version") as String

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
}

checkstyle {
    toolVersion = "10.23.1"
    configDirectory.set(layout.projectDirectory.dir("config/checkstyle"))
    isIgnoreFailures = false
    maxWarnings = 0
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.34.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf("-Xlint:deprecation", "-Xlint:unchecked", "-Xlint:rawtypes")
    )
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

application {
    mainClass.set("dev.klomptech.jbark.Bark")
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("all")
    archiveBaseName.set("bark")
    manifest {
        attributes("Main-Class" to "dev.klomptech.jbark.Bark")
    }
}

tasks.register<Copy>("dist") {
    group = "distribution"
    description = "JAR plus bark / bark.cmd launchers in build/dist/"
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar)
    from("bin/bark", "bin/bark.cmd")
    into(layout.buildDirectory.dir("dist"))
    doLast {
        layout.buildDirectory.file("dist/bark").get().asFile.setExecutable(true)
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.check {
    dependsOn("spotlessCheck")
}

tasks.register("lint") {
    group = "verification"
    description = "Format check, style check, and unit tests"
    dependsOn("spotlessCheck", "check")
}

publishing {
    repositories {
        maven {
            name = "buildDir"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        "io.github.alexh89",
        "bark",
        version.toString()
    )

    pom {
        name.set("Bark")
        description.set("A dog-themed esoteric story programming language (JBark interpreter)")
        url.set("https://github.com/AlexH89/bark")

        licenses {
            license {
                name.set("GNU Affero General Public License v3.0 or later")
                url.set("https://www.gnu.org/licenses/agpl-3.0.html")
            }
        }

        developers {
            developer {
                id.set("AlexH89")
                name.set("Alex Hovenkamp")
                url.set("https://github.com/AlexH89")
            }
        }

        scm {
            url.set("https://github.com/AlexH89/bark")
            connection.set("scm:git:https://github.com/AlexH89/bark.git")
            developerConnection.set("scm:git:ssh://github.com/AlexH89/bark.git")
        }
    }
}

signing {
    useGpgCmd()
}
