node('jslave-cockpit-machines') {
    stage('Clean workspace'){
        cleanWs()
    }
    
    stage('Get ansible scripts') {
        checkout([
            $class: 'GitSCM',
            branches: [[name: '*/main']],
            extensions: [
                 [$class: 'CloneOption', depth: 1, noTags: true, shallow: true],
            ],
            userRemoteConfigs: [[url: 'https://github.com/yunmingyang/cockpit-machines-env-setup.git']],
        ])
    }

    stage('Get Pub key used by automation') {
        dir('bots'){
            checkout([
                $class: 'GitSCM',
                branches: [[name: '*/main']],
                extensions: [
                    [$class: 'CloneOption', depth: 1, noTags: true, shallow: true],
                    [$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [[$class:'SparseCheckoutPath', path:'machine/identity.pub']]]],
                userRemoteConfigs: [[url: 'https://github.com/cockpit-project/bots.git']],
            ])
        }
    }

    stage('Setup the environment') {
        def cmd = "source ${ANSIBLEENV} && " +\
                  "ansible-playbook -v -i inventory main.yml " +\
                  "-e machine1=${HOST} " +\
                  "-e image_url=${IMAGEURL} " +\
                  "-e authorized_path=\"bots/machine/identity.pub\""
        sh(cmd)
    }
}