
description = "doma-bom"

dependencies {
    constraints {
        api(project(":doma-core"))
        api(project(":doma-kotlin"))
        api(project(":doma-mock"))
        api(project(":doma-processor"))
        api(project(":doma-slf4j"))
        api(project(":doma-template"))
    }
}
