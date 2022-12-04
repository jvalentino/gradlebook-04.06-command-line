package com.blogspot.jvalentino.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Does a trace route
 * @author jvalentino2
 */
@SuppressWarnings(['Println', 'DuplicateNumberLiteral'])
class PingTask extends DefaultTask {

    static final String PING = 'ping'
    protected PingTask instance = this

    @TaskAction
    void perform() {
        boolean isWindows = OsUtil.isWindows()

        List<String> commands = instance.generateCommand(
                isWindows, instance.project.properties['host'])

        String output = instance.executeCommand(commands)

        List<Double> times = instance.parsePingOutput(
                isWindows, output)

        println times
    }

    List<String> generateCommand(boolean isWindows, String host) {
        List<String> commands = []
        String pingCount = '4'

        if (isWindows) {
            commands.add('cmd')
            commands.add('/c')
            commands.add(PING)
            commands.add('-n')
        } else {
            commands.add(PING)
            commands.add('-c')
        }

        commands.add(pingCount)
        commands.add(host)

        commands
    }

    String executeCommand(List<String> commands) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream()

        instance.project.exec {
            commandLine commands
            standardOutput = stdout
        }

        stdout.toString()
    }

    List<Double> parsePingOutput(boolean isWindows, String output) {
        String whitespace = '\\s+'
        String pingSplit = '='

        List<Double> results = []
        if (isWindows) {
            output.eachLine { String line ->
                String[] split = line.trim().split(whitespace)

                if (split.first().startsWith('Reply')) {
                    String timeString =
                            split[4].split(pingSplit).last()
                    timeString = timeString[0..-3]
                    double time = Double.parseDouble(timeString)
                    results.add(time)
                }
            }
        } else {
            output.eachLine { String line ->
                String[] split = line.trim().split(whitespace)

                if (split.length == 8) {
                    double time = Double.parseDouble(
                            split[6].split(pingSplit).last())
                    results.add(time)
                }
            }
        }

        results
    }
}
