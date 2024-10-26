plugins {
    `module-lib`
    `lib`
}

dependencies {
    api(project(":txrpc-body-common"))
    api("com.esotericsoftware:kryo:5.6.2")
}
