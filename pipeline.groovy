node(env.NODE_GROUP) {
    stage('Initialization') {
        deleteDir()
        checkout([$class: 'GitSCM', branches: [[name: '*/master']],
            userRemoteConfigs: [[url: 'https://github.com/ColFouks/CICD', credentialsId: "GitHub", branch: "master"]]])
         def rawJC = env.JC.decodeBase64()
         JC = new ConfigSlurper().parse(rawJC)
    }
    
}
