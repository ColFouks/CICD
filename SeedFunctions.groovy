class SeedFunctions {
    static generateJobName(jc) {
        def folderedBaseName

        folderedBaseName = [
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
}
