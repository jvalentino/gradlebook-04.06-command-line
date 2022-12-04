package com.blogspot.jvalentino.gradle

import java.util.List

import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.impldep.com.google.common.collect.ImmutableMap
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class PingTaskTestSpec extends Specification {
    @Subject
    PingTask task
    Project project
    
    def setup() {
        Project p = ProjectBuilder.builder().build()
        task = p.task('ping', type: PingTask)
        task.instance = Mock(PingTask)
        project = Mock(ProjectInternal)
        GroovyMock(OsUtil, global:true)
    }
    
    void "unit test perform"() {
        given:
        
        List<String> commands = ['a', 'b']
        Map properties = ['host':'foo.com']
        
        when:
        task.perform()
        
        then:
        1 * OsUtil.isWindows() >> true
        1 * task.instance.project >> project
        1 * project.properties >> properties
        1 * task.instance.generateCommand(true, 'foo.com') >> commands
        1 * task.instance.executeCommand(commands) >> 'output'
        1 * task.instance.parsePingOutput(true, 'output') >> [32.43]
    }
    
    @Unroll
    void "test generateCommand for windows=#isWindows"() {
        given:
        String host = "foo.com"
        
        when:
        List<String> commands = task.generateCommand(isWindows, host)
        
        then:
        commands.toString() == result
        
        where:
        isWindows   || result
        true        || '[cmd, /c, ping, -n, 4, foo.com]'
        false       || '[ping, -c, 4, foo.com]'
    }
    
    void "test executeCommand"() {
        given:
        GroovyMock(ByteArrayOutputStream, global:true)
        ByteArrayOutputStream os = Mock(ByteArrayOutputStream)
        List<String> commands = ['a', 'b']
        
        when:
        String output = task.executeCommand(commands)
        
        then:
        1 * task.instance.project >> project
        1 * project.exec(_)
        1 * new ByteArrayOutputStream() >> os
        1 * os.toString() >> 'output'
        
        and:
        output == 'output'
    }
    
    void "Test parsePingOutput for not windows"() {
        given:
        String output = 
            new File('src/test/resources/ping-linux.txt').text
        
        when:
        List<Double> results = task.parsePingOutput(false, output)
        
        then:
        results.toString() == '[59.057, 50.334, 45.259, 44.93]'
    }
    
    void "Test parsePingOutput for windows"() {
        given:
        String output = 
            new File('src/test/resources/ping-windows.txt').text
        
        when:
        List<Double> results = task.parsePingOutput(true, output)
        
        then:
        results.toString() == '[68.0, 68.0, 65.0]'
    }
}
