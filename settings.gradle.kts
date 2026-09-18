pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("io.github.ben-manes.versions.settings") version "0.61.0"
}

rootProject.name = "txrpc"

fun add(name: String) {
    val mname = "modules/$name"
    include(mname)
    project(":$mname").name = "txrpc-$name"
}

add("api")
add("runtime")

add("body-common")
add("body-client")
add("body-server")
add("body-kryo")

add("remote-common")
add("remote-client")
add("remote-server")
