import SeedFunctions


class MavenBuild {
    static job (dslFactory, jobConfig) {
        dslFactory.job(SeedFunctions.generateJobNameAndFolder(dslFactory, jobConfig)) {
            jobConfig."maven.profiles" = jobConfig."maven.profiles" + ["\$${jobConfig."job.profileParamName"}"]
            parameters {
                        stringParam(jobConfig.'maven.versionParamName', "", "Artifact version to set")
                        stringParam(jobConfig.'maven.shaParamName', "", "SHA to checkout from")
                        if (jobConfig.'maven.availableProfiles' && jobConfig.'job.profileParamName') {
                            choiceParam(jobConfig.'job.profileParamName', jobConfig.'maven.availableProfiles', 'Please Select PROFILE')
                        }
                    }            
            scm {
                git {
                    remote { url("${jobConfig.'github.user'}@${jobConfig.'github.host'}:${jobConfig.'github.org'}/${jobConfig.'github.repo'}") }
                    branch("\$${jobConfig.'maven.shaParamName'}")
                }
            }            
            steps {
                    jobConfig.'maven.steps' = "versions:set"
                    jobConfig.'maven.nonCodeBuild' = true
                    jobConfig.'maven.extraParams' = "-DnewVersion=\${${jobConfig.'maven.versionParamName'}}"
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
                    if (jenkins.mvn.FilePathSettingsProvider){
                        configure { maven ->
                            maven / 'settings'(class: "jenkins.mvn.FilePathSettingsProvider") {
                                path(jobConfig.'maven.mvnConfigFilePath')
                            }
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
