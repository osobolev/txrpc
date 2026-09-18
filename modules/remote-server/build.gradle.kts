plugins {
    id("module-lib")
    id("lib")
}

dependencies {
    api(project(":txrpc-remote-common"))
    api(project(":txrpc-runtime"))
}
