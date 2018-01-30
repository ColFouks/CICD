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
            dslFactory.out.println("we are here")
            jobClass.job(dslFactory, jc)
        }
        return allJCs
    }

    def runSeed(jc) {
        processJCFile(jc)
    }
}