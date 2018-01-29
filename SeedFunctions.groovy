class SeedFunctions {
    static generateJobName(jc) {
        def folderedBaseName

        folderedBaseName = [
                (jc.'job.isProductionSeed')
                        ? jc.'folder.rootDirectory'
                        : "${jc.'folder.rootDirectory'}/${jc.'folder.program'}/X_CICD_Development",
                jc.'folder.program' ?: "",
                jc.'folder.projectGroup'?: "",
                jc.'folder.project'?: "",
                jc.'folder.jobType'?: "",
                jc.'job.baseName'].findAll { it != null && it.toString().length() != 0 }.join("/")

        def result = folderedBaseName
        return result
    }
    private static folderRecursive(dslFactory, fold) {
        def list = fold.split("/").toList()
        def folderName = "${list[0]}"
        for (String item : list.drop(1)) {
            folderName = folderName + "/" + item
            dslFactory.folder(folderName)
        }
    }

    static generateJobNameAndFolder(dslFactory, jobConfig) {
        def fullJobName = generateJobName(jobConfig)
        def folderPath = fullJobName.tokenize('/').dropRight(1).join('/')

        folderRecursive(dslFactory, folderPath)

        return fullJobName
    }

    static loadJobClass(jc) {
        return Class.forName("${jc.'jobClass.baseClassName'}")?.newInstance()
    }

    static parseProjectMapClosure() {
        return """{ mapAsString ->  mapAsString[1..-2].split(', ').collectEntries { entry -> def pair = entry.split(':')
            [(pair.first().toString().trim()): pair.last().toString().trim()] }        
        }"""
    }

}
