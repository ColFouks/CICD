
import SeedFunctions
import jobdsl.utilities.JobConfigurationItemsCollection

class ExecuteSeed {
    static job(dslFactory, jobConfig) {
        dslFactory.job(SeedFunctions.generateJobNameAndFolder(dslFactory, jobConfig)) {
            steps {
                dsl {
                    text("""new SeedExecutor(this).runSeed()""")
                    additionalClasspath("./")
                }
            }
        }
    }
}
