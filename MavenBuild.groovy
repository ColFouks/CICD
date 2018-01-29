package jobdsl.build.job

import SeedFunctions


class MavenBuild {
    static job (dslFactory, jobConfig) {
        dslFactory.job(SeedFunctions.generateJobNameAndFolder(dslFactory, jobConfig)) {
            jobConfig."maven.profiles" = jobConfig."maven.profiles" + ["\$${jobConfig."job.profileParamName"}"]
            steps {
                    jobConfig.'maven.steps' = "versions:set"
                    jobConfig.'maven.nonCodeBuild' = true
                maven {
                    goals("""
                        -e
                        -U
                        -B
                        ${jobConfig.'maven.steps'}
                        -Djava.17.home=\${JAVA_HOME}
                        ${jobConfig.'maven.extraParams'}
                        """.stripIndent())
                    mavenInstallation(jobConfig.'jenkins.mvnLabel')
                    rootPOM(jobConfig.'maven.pomFile')
                    configure { maven ->
                        maven / 'settings'(class: "jenkins.mvn.FilePathSettingsProvider") {
                            path(jobConfig.'maven.mvnConfigFilePath')
                        }
                    }
                }
                shell("""
                        set +x
                        echo "Full Artifact version: \${${jobConfig.'maven.versionParamName'}}"
                        snapshot=\$(echo \${${ jobConfig.'maven.versionParamName' }} | cut -d '-' -f1)
                        echo "Snapshot version: \${snapshot}-SNAPSHOT"
                        """)
            }
        }
    }
}
