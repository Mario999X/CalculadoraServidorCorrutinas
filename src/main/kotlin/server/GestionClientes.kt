package server

import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

class GestionClientes(private val socket: Socket) {

    // Canal de entrada y de salida
    private val entrada = socket.openReadChannel()
    private val salida = socket.openWriteChannel(true) // true, para que se envíe el dato al instante

    suspend fun run() = withContext(Dispatchers.IO) { // Importante pasar el contexto

        val procesarOperacion = launch {
            log.debug { "Recibiendo operacion" }

            // Pasar a JSON el objeto operacion
        }
    }
}