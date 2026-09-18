plugins {
    id("module-lib")
    id("lib")
}

dependencies {
    api(project(":txrpc-body-common"))
    api(project(":txrpc-remote-client"))
}
