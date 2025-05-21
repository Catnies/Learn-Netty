// 插件
plugins {
    id("java") // Java
}


// 项目信息
group = "top.catnies"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21


// 依赖
repositories {
    // 开发工具
    mavenCentral()
}

dependencies {
    // 开发工具
    compileOnly("org.projectlombok:lombok:1.18.34") // Lombok
    annotationProcessor("org.projectlombok:lombok:1.18.34") // Lombok
    compileOnly("io.netty:netty-all:4.1.119.Final")  // Netty
}