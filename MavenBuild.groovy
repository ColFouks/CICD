import SeedFunctions


class MavenBuild {
    static job (dslFactory, jobConfig) {
        def folderedBaseName
        folderedBaseName = [
                jobConfig.'folder.project'?: "",
                jobConfig.'folder.jobType'?: "",
                jobConfig.'job.baseName'].findAll { it != null && it.toString().length() != 0 }.join("/")
                
        def folderPath = folderedBaseName.tokenize('/').dropRight(1).join('/')
        
        def list = folderPath.split("/").toList()
        def folderName = "${list[0]}"
        for (String item : list.drop(1)) {
            folderName = folderName + "/" + item
            dslFactory.folder(folderName)
        }        
        
        dslFactory.job(folderedBaseName) {
            jobConfig."maven.profiles" = jobConfig."maven.profiles" + ["\$${jobConfig."job.profileParamName"}"]
            parameters {
                        stringParam(jobConfig.'maven.shaParamName', "", "SHA to checkout from")
                        if (jobConfig.'maven.availableProfiles' && jobConfig.'job.profileParamName') {
                            choiceParam(jobConfig.'job.profileParamName', jobConfig.'maven.availableProfiles', 'Please Select PROFILE')
                        }
                    }            
            scm {
                git {
                    remote { url("https://${jobConfig.'github.org'}/${jobConfig.'github.repo'}") }
                    credentials('GitHub')
                    branch("\$${jobConfig.'maven.shaParamName'}")
                }
            }            
            steps {
                    jobConfig.'maven.steps' = "versions:set"
                    jobConfig.'maven.nonCodeBuild' = true
                    jobConfig.'maven.extraParams' = "-DnewVersion=\${${jobConfig.'maven.versionParamName'}}"
                    
                    
                systemGroovyCommand("""
                import hudson.model.*

                def build = this.getProperty('binding').getVariable('build')
                def listener = this.getProperty('binding').getVariable('listener')
                def env = build.getEnvironment(listener)
                sha = env.GIT_COMMIT
                buildNum = env.BUILD_NUMBER
                hudson.FilePath workspace = hudson.model.Executor.currentExecutor().getCurrentWorkspace()
                File f = new File("${workspace}/pom.xml")
                def inputFileText = f.getText()
                def gitSha = sha.substring(0, 7)
                def dateNow = new Date().format('yyyyMMdd.HHmmss')

                def data = new groovy.util.XmlSlurper().parseText(inputFileText)
                def v = data.version.toString() - ".0.0-SNAPSHOT"
                println v 
                if (v != data.version.toString()) {
                    v += ".$buildNum.$gitSha-SNAPSHOT"
                }
                def pa = new ParametersAction([new StringParameterValue("${jobConfig.'maven.versionParamName'}", v)], ["${jobConfig.'maven.versionParamName'}"])
                build.addAction(pa)
                """)
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
