import java.util.regex.Pattern

def rootDir = new File(request.getOutputDirectory() + "/" + request.getArtifactId())
def coreBundle = new File(rootDir, "core")

def uiAppsPackage = new File(rootDir, "ui.apps")
def uiAppsPom = new File(uiAppsPackage, "pom.xml")
def allPackage = new File(rootDir, "all")
def rootPom = new File(rootDir, "pom.xml")
def readme = new File(rootDir, "Readme.md")
def readmeAll = new File(rootDir, "Readme.All.md")
def readmeNotAll = new File(rootDir, "Readme.NotAll.md")

def optionAll = request.getProperties().get("optionAll")


// helper methods

// Remove the given Module from the parent POM
def removeModule(pomFile, moduleName) {
    def pattern = Pattern.compile("\\s*<module>" + Pattern.quote(moduleName) + "</module>", Pattern.MULTILINE)
    def pomContent = pomFile.getText("UTF-8")
    pomContent = pomContent.replaceAll(pattern, "")
    pomFile.newWriter().withWriter { w ->
        w << pomContent
    }
}

// Either remove the tag lines or the line plus the content in between
// forAll = true: removes all content between @startForNotAll@ and @endForNotAll@
// forAll = false: emoves all content between @startForAll@ and @endForAll@
def removeTags(pomFile, forAll) {
    if(!forAll) {
        // Remove all lines for Not All and remove all content inside for All
        def startPattern = Pattern.compile("\\s*<!-- @startForNotAll@ .*-->")
        def endPattern = Pattern.compile("\\s*<!-- @endForNotAll@ .*-->")
        def wrapPattern = Pattern.compile("\\s*<!-- @startForAll@ [\\s\\S]*?<!-- @endForAll@ .*-->")

        def pomContent = pomFile.getText("UTF-8")
        pomContent = pomContent.replaceAll(startPattern, "")
        pomContent = pomContent.replaceAll(endPattern, "")
        pomContent = pomContent.replaceAll(wrapPattern, "")
        pomFile.newWriter().withWriter { w ->
            w << pomContent
        }
    } else {
        // Remove all lines for All and remove all content inside for Not All
        def wrapPattern = Pattern.compile("\\s*<!-- @startForNotAll@ [\\s\\S]*?<!-- @endForNotAll@ .*-->")
        def startPattern = Pattern.compile("\\s*<!-- @startForAll@ .*-->")
        def endPattern = Pattern.compile("\\s*<!-- @endForAll@ .*-->")

        def pomContent = pomFile.getText("UTF-8")
        pomContent = pomContent.replaceAll(wrapPattern, "")
        pomContent = pomContent.replaceAll(startPattern, "")
        pomContent = pomContent.replaceAll(endPattern, "")
        pomFile.newWriter().withWriter { w ->
            w << pomContent
        }
    }
}

if(optionAll == "n") {
    // Remove All Package / Module
    assert allPackage.deleteDir()
    removeModule(rootPom, "all")
    // Remove content for 'All' and remove tag lines for Not All
    removeTags(uiAppsPom, false)
    // Delete the Readme.md for All
    assert readmeAll.delete()
    // Rename the Not For All Readme to the Readme.md file
    assert readmeNotAll.renameTo(readme)
} else {
    // Remove content for 'Not All' and remove tag lines for All
    removeTags(uiAppsPom, true)
    // Delete the Readme.md for Not All
    assert readmeNotAll.delete()
    // Rename the For All Readme to the Readme.md file
    assert readmeAll.renameTo(readme)
}