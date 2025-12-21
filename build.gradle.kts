plugins {
    id("java")
}

group = "com.keplersj.golem"
version = "1.0.0"

subprojects {
    apply(plugin = "java")
    
    group = "com.keplersj.golem"
    version = "1.0.0"
    
    repositories {
        mavenCentral()
    }
}
