task("webpack", Exec::class) {
    inputs.file("package-lock.json")
    inputs.dir("src")
    inputs.file("webpack.config.js")
    outputs.dir("$buildDir/js")

    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
        // I have no clue why invoking it directly does not work on my system
        commandLine("cmd", "/c", "$projectDir/node_modules/.bin/webpack")
    } else {
        val nvmDir = System.getenv("HOME") + "/.nvm"
        val nodeVersion = "v16.20.2"
        val nvmScript = "$nvmDir/nvm.sh"
        commandLine("$nvmScript use $nodeVersion")
        commandLine("${nvmDir}/versions/node/$nodeVersion/bin/node", "$projectDir/node_modules/webpack/bin/webpack.js")
    }
}.dependsOn(":columns-parser:jsBrowserProductionLibraryDistribution")

