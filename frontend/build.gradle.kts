task("webpack", Exec::class) {
    inputs.file("package-lock.json")
    inputs.dir("src")
    inputs.file("webpack.config.js")
    outputs.dir("$buildDir/js")

    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
        // I have no clue why invoking it directly does not work on my system
        commandLine("cmd", "/c", "$projectDir/node_modules/.bin/webpack")
    } else {
        commandLine("$projectDir/node_modules/.bin/webpack")
    }
}.dependsOn(":columns-parser:jsBrowserProductionLibraryDistribution")

