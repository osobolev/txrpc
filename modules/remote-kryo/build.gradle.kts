plugins {
    `module-lib`
    `lib`
}

dependencies {
    api(project(":txrpc-remote-common"))
    api("com.esotericsoftware:kryo:5.6.0")
}
