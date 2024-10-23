plugins {
    `module-lib`
    `lib`
}

dependencies {
    api(project(":txrpc-body-common"))
    api(project(":txrpc-remote-client"))
}
