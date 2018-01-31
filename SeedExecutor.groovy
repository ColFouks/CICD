import ConfigProcessor

class SeedExecutor {

    private dslFactory

    SeedExecutor(dslFactory) {
        this.dslFactory = dslFactory
    }

    def processJCFile(jcFile) {
        def configProcessor = new ConfigProcessor(dslFactory)
        def allJCs = configProcessor.processConfig(jcFile)
        def allJobsMap = configProcessor.allJobs
        
        allJCs.each { jc ->
            configProcessor.prettyPrint(jc)        
            def jobClass = Class.forName("${jc.'jobClass.baseClassName'}")?.newInstance()
            def nonFlatJC = configProcessor.nonFlatJC
            allJobsMap.each { k,v -> nonFlatJC."allJobs.${k}" = v }
            jobClass.job(dslFactory, jc, nonFlatJC)
        }
        return allJCs
    }

    def runSeed(jc) {
        processJCFile(jc)
    }
}