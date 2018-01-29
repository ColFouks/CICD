import ConfigProcessor

class SeedExecutor {

    private dslFactory

    SeedExecutor(dslFactory) {
        this.dslFactory = dslFactory
    }

    def processJCFile(jcFile) {
        def configProcessor = new ConfigProcessor(dslFactory)
        def allJCs = configProcessor.processConfig(jcFile)
        def allJobsMap = [:]

        allJCs.each { jc ->
            def fullJobName = SeedFunctions.generateJobName(ConfigProcessor.clone(jc))
            allJobsMap[jc.'job.baseName'] = fullJobName
            allJobsMap.each { k,v -> jc."allJobs.${k}" = v }
            configProcessor.prettyPrint(jc)            
            jobClass.job(dslFactory, jc)
        }
        return allJCs
    }

    def runSeed(jc) {
        processJCFile(jc)
    }
}