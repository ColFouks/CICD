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
            configProcessor.prettyPrint(jc)        
            def jobClass = Class.forName("${jc.'jobClass.baseClassName'}")?.newInstance()
            allJobsMap.each { k,v -> nonFlatJC."allJobs.${k}" = v }
            allJobsMap.each { k,v -> jc."allJobs.${k}" = v }
            def nonFlatJC = configProcessor.nonFlatJC
            jobClass.job(dslFactory, jc, nonFlatJC)
        }
        return allJCs
    }

    def runSeed(jc) {
        processJCFile(jc)
    }
}