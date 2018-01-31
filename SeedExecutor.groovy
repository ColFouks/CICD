import ConfigProcessor

class SeedExecutor {

    private dslFactory

    SeedExecutor(dslFactory) {
        this.dslFactory = dslFactory
    }

    def processJCFile(jcFile) {
        def configProcessor = new ConfigProcessor(dslFactory)
        def allJCs = configProcessor.processConfig(jcFile)
        def allJobs = [:]
        allJCs.each { jc ->
            def folderedBaseName = [
                jc.'folder.project'?: "",
                jc.'folder.jobType'?: "",
                jc.'job.baseName'].findAll { it != null && it.toString().length() != 0 }.join("/")
            allJobs[folderedBaseName.tokenize('/')[-1]] = folderedBaseName
        }
        allJCs.each { jc ->
            configProcessor.prettyPrint(jc)        
            def jobClass = Class.forName("${jc.'jobClass.baseClassName'}")?.newInstance()
            def nonFlatJC = configProcessor.nonFlatJC
            nonFlatJC.allJobs = [:]
            allJobs.each { k,v -> nonFlatJC."allJobs.${k}" = v }            
            dslFactory.out.println(nonFlatJC)
            jobClass.job(dslFactory, jc, nonFlatJC)
        }
        return allJCs
    }

    def runSeed(jc) {
        processJCFile(jc)
    }
}