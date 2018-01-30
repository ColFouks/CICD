import ConfigProcessor

class SeedExecutor {

    private dslFactory

    SeedExecutor(dslFactory) {
        this.dslFactory = dslFactory
    }

    def processJCFile(jcFile) {
        def configProcessor = new ConfigProcessor(dslFactory)
        def allJCs = configProcessor.processConfig(jcFile)
        
        allJCs.each { jc ->
            def folderedBaseName
            folderedBaseName = [
                jc.'folder.project'?: "",
                jc.'folder.jobType'?: "",
                jc.'job.baseName'].findAll { it != null && it.toString().length() != 0 }.join("/")           
            allJobsMap[jc.'job.baseName'] = folderedBaseName
            configProcessor.prettyPrint(jc)        
            def jobClass = Class.forName("${jc.'jobClass.baseClassName'}")?.newInstance()
            jobClass.job(dslFactory, jc)
        }
        return allJCs
    }

    def runSeed(jc) {
        processJCFile(jc)
    }
}