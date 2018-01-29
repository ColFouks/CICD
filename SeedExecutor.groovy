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
            dslFactory.out.println(fullJobName)
            allJobsMap[jc.'job.baseName'] = fullJobName
            configProcessor.prettyPrint(jc)
        }

        allJCs.each { jc ->
            allJobsMap.each { k,v -> jc."allJobs.${k}" = v }
        }

        return allJCs
    }

    def runSeed(jc) {
        processJCFile(jc)
    }
}