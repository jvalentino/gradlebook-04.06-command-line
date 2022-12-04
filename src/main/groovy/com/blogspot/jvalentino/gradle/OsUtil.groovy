package com.blogspot.jvalentino.gradle

import org.gradle.internal.os.OperatingSystem

/**
 * Since I can't figure out how to mock OperatingSystem,
 * I threw it in this class
 * so at least the wrapping class can be mocked
 * @author jvalentino2
 */
class OsUtil {

    static boolean isWindows() {
        OperatingSystem.current().isWindows()
    }
}
