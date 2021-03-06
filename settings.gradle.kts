rootProject.name = "smithy"
include(":smithy-aws-traits")
include(":smithy-aws-apigateway-openapi")
include(":smithy-aws-protocol-tests")
include(":smithy-cli")
include(":smithy-codegen-core")
include(":smithy-codegen-freemarker")
include(":smithy-build")
include(":smithy-model")
include(":smithy-diff")
include(":smithy-linters")
include(":smithy-mqtt-traits")
include(":smithy-jsonschema")
include(":smithy-openapi")
include(":smithy-utils")
include(":smithy-protocol-test-traits")

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
