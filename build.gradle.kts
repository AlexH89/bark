import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    `maven-publish`
    checkstyle
    id("com.diffplug.spotless") version "7.0.4"
    id("com.gradleup.shadow") version "9.0.2"
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
    withJavadocJar()
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
        googleJavaFormat("1.25.2")
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
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("Bark")
                description.set("A dog-themed esoteric story programming language (JBark interpreter)")
                url.set("https://github.com/AlexH89/bark")
                licenses {
                    license {
                        name.set("GNU Affero General Public License v3.0 or later")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.html")
                        distribution.set("repo")
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
                    connection.set("scm:git:git://github.com/AlexH89/bark.git")
                    developerConnection.set("scm:git:ssh://github.com:AlexH89/bark.git")
                    url.set("https://github.com/AlexH89/bark")
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/AlexH89/bark/issues")
                }
            }
        }
    }
    repositories {
        maven {
            name = "buildDir"
            url = uri(layout.buildDirectory.dir("repo"))
        }
        // Maven Central: add the signing plugin and a Sonatype repository block
        // once centralUsername / centralPassword are set in ~/.gradle/gradle.properties.
    }
}
