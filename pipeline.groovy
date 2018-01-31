node(env.NODE_GROUP) {
    stage('Initialization') {
        deleteDir()
        checkout([$class: 'GitSCM', branches: [[name: '*/master']],
            userRemoteConfigs: [[url: 'https://github.com/ColFouks/CICD', credentialsId: "GitHub", branch: "master"]]])
         def rawJC = readFile "${env.WORKSPACE}/test.jc"
         JC = new ConfigSlurper().parse(rawJC)
    }
    
}
