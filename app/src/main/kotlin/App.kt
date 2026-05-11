import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging


private val logger = KotlinLogging.logger {}

fun main() {
    Log().info { "Welcome to the Kurome!" }

}

fun Log(): KLogger {
    return logger
}