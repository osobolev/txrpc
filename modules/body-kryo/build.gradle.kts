plugins {
    id("module-lib")
    id("lib")
}

dependencies {
    api("com.esotericsoftware:kryo:5.6.2")
    api(project(":txrpc-body-common"))
}
