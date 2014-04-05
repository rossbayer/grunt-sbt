module.exports = function(grunt) {

  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-copy');

  // Project configuration.
  grunt.initConfig({
    concat: {
      options: {
        stripBanners: true,
        banner: '(function() {',
        footer: '})();'
      },
      dist: {
        src: ['src/main/javascript/**/*.js'],
        dest: 'target/grunt/math-lib.js'
      }
    },
    copy: {
      dist: {
        files: [
          {
            expand: true,
            src: ['src/main/javascript/**/*.js'],
            dest: 'target/grunt/scripts/files',
            flatten: true
          }
        ]
      }
    }
  });

  grunt.registerTask("build", ['concat', 'copy']);

  grunt.registerTask("test", function() {
    grunt.log.writeln("Grunt test success!");
  });
};