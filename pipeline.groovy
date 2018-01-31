node(env.NODE_GROUP) {
    def JC
    stage('Initialization') {
        deleteDir()
        checkout([$class: 'GitSCM', branches: [[name: '*/master']],
            userRemoteConfigs: [[url: 'https://github.com/ColFouks/CICD', credentialsId: "GitHub", branch: "master"]]])
        def rawJC = env.JC.decodeBase64()
        rawJC = new String(rawJC)
        JC = new groovy.json.JsonSlurperClassic().parseText(rawJC)
    }
    for (int i = 0; i < JC.pipeline.size(); i++) {
        def pipeJob = JC.pipeline[i]

        def pipelineParameters = []
        for (int j; j < pipeJob.size(); j++){
            pipelineParameters.add([$class: 'StringParameterValue', name: j.key, value: j.value])
        }        
        build job: pipeJob, parameters: pipelineParameters

    }
    
}
