class Pipeline {
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

        dslFactory.pipelineJob(folderedBaseName) {
            jobConfig."maven.profiles" = jobConfig."maven.profiles" + ["\$${jobConfig."job.profileParamName"}"]
            parameters {
                        stringParam(jobConfig.'maven.shaParamName', "", "SHA to checkout from")
                        if (jobConfig.'maven.availableProfiles' && jobConfig.'job.profileParamName') {
                            choiceParam(jobConfig.'job.profileParamName', jobConfig.'maven.availableProfiles', 'Please Select PROFILE')
                        }
                    } 
            environmentVariables {
                env("JC", jobConfig.toString().bytes.encodeBase64().toString())
            }
            scm {
                git {
                    remote { 
                        url("https://github.com/${jobConfig.'github.org'}/${jobConfig.'github.repo'}") 
                        credentials('GitHub')
                    }
                    branch("\$${jobConfig.'maven.shaParamName'}")
                }
                def wfFile = delegate.jobManagement.readFileInWorkspace('pipeline.groovy')
                definition {
                    cps {
                        script(wfFile)
                    }
                }
            }            
        }
    }
}