plugins {
    `module-lib`
    `lib`
}

dependencies {
    api(project(":txrpc-remote-common"))
    api(project(":txrpc-runtime"))
}
