//
module.exports = function(grunt) {
    //
    var externalComponentsPath = 'src/main/webapp/static/external-components';

    grunt.initConfig({
        clean: {
            'node': ['node_modules'],
            'web-deps': ['bower_components', externalComponentsPath]
        },

        bower: {
            install: {
                options: {
                    targetDir: externalComponentsPath,
                    layout: 'byComponent',
                    install: true,
                    verbose: true,
                    cleanTargetDir: true,
                    cleanBowerDir: false,
                    bowerOptions: {
                        forceLatest: true,
                        production: false
                    }
                }
            }
        }
    });

    //
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-bower-task');


    grunt.registerTask('cleanup', ['clean']);
    grunt.registerTask('init', ['clean:web-deps', 'bower']);
};
