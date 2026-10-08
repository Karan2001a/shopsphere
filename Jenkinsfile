
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test EC2 Connection') {
    steps {
        sshagent(credentials: ['shopsphere-ec2-ssh']) {
            bat '''
                "C:\\Program Files\\Git\\usr\\bin\\ssh.exe" -o BatchMode=yes -o KexAlgorithms=curve25519-sha256 -o StrictHostKeyChecking=yes -o UserKnownHostsFile="C:\\ProgramData\\Jenkins\\.jenkins\\.ssh\\known_hosts" ubuntu@35.183.122.148 "docker --version"
            '''
        }
    }
}
    }

    post {
        success {
            echo 'Jenkins connected to AWS EC2 successfully!'
        }
        failure {
            echo 'EC2 SSH connection failed. Check Console Output.'
        }
    }
}
