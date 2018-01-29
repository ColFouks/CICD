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
            //def jobClass = SeedFunctions.loadJobClass(jc)
            def fullJobName = SeedFunctions.generateJobName(ConfigProcessor.clone(jc))
            allJobsMap[jc.'job.baseName'] = fullJobName
            allJobsMap.each { k,v -> jc."allJobs.${k}" = v }
            configProcessor.prettyPrint(jc)        
            dslFactory.out.println("${(jc.'jobClass.classPath').replaceAll("/", ".")}.${jc.'jobClass.baseClassName'}")
            //jobClass.job(dslFactory, jc)
        }
        return allJCs
    }

    def runSeed(jc) {
        processJCFile(jc)
    }
}