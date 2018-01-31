node(env.NODE_GROUP) {
    def JC
    stage('Initialization') {
        deleteDir()
        checkout([$class: 'GitSCM', branches: [[name: '*/master']],
            userRemoteConfigs: [[url: 'https://github.com/ColFouks/CICD', credentialsId: "GitHub", branch: "master"]]])
        def rawJC = env.JC.decodeBase64()
        rawJC = new String(rawJC)
        JC = new groovy.json.JsonSlurperClassic().parseText(rawJC)
        echo JC.toString()
    }
    for (kv in mapToList(JC.job.pipeline)) {
        def pipeJob = kv[0] 
        def pipelineParameters = []
        for (paramskv in mapToList(kv[1])) {
            pipelineParameters.add([$class: 'StringParameterValue', name: paramskv[0], value: paramskv[1]])
        }
        build job: JC.allJobs.pipeJob, parameters: pipelineParameters
    }
}
@NonCPS
List<List<?>> mapToList(Map map) {
  return map.collect { it ->
    [it.key, it.value]
  }
}