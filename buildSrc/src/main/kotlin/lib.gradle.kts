import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    id("base-lib")
    id("com.vanniktech.maven.publish")
}

group = "io.github.osobolev.txrpc"
version = "2.6"

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("${project.group}", "${project.name}", "${project.version}")
    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = SourcesJar.Sources()
    ))
}

mavenPublishing.pom {
    name.set("${project.group}:${project.name}")
    description.set("RPC that can do multiple remote calls within one transaction")
    url.set("https://github.com/osobolev/txrpc")
    licenses {
        license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
    developers {
        developer {
            name.set("Oleg Sobolev")
            organizationUrl.set("https://github.com/osobolev")
        }
    }
    scm {
        connection.set("scm:git:https://github.com/osobolev/txrpc.git")
        developerConnection.set("scm:git:https://github.com/osobolev/txrpc.git")
        url.set("https://github.com/osobolev/txrpc")
    }
}
