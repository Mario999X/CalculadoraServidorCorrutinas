package server

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import model.Operacion
import model.mensajes.Request
import mu.KotlinLogging

private val log = KotlinLogging.logger {}
private val json = Json

class GestionClientes(private val socket: Socket) {

    private var result = 0
    var respuesta = ""

    // Canal de entrada y de salida
    private val entrada = socket.openReadChannel()
    private val salida = socket.openWriteChannel(true) // true, para que se envíe el dato al instante

    suspend fun run() = withContext(Dispatchers.IO) { // Importante pasar el contexto

        val procesarOperacion = launch {
            log.debug { "Recibiendo operacion" }

            // Recibimos el dato del cliente
            val input = entrada.readUTF8Line()
            log.debug { "Se ha recibido un mensaje: $input" }

            // json recogido
            input?.let {
                // Lo decodificamos
                val request = json.decodeFromString<Request<Operacion>>(input)
                //println(request)

                // Segun el tipo de request, se ejecutara una parte del codigo o otra (solo 1 caso en este problema)
                when (request.type) {
                    Request.Type.SEND -> {
                        // Al recibir este tipo, se pilla el contenido y se transforma en una Operacion
                        log.debug { "Se ha recibido una operacion... " }
                        val op = request.content as Operacion

                        result = if (op.operador.uppercase() == "SUMA" || op.operador == "+") {
                            op.num1 + op.num2
                        } else if (op.operador.uppercase() == "RESTA" || op.operador == "-") {
                            op.num1 - op.num2
                        } else if (op.operador.uppercase() == "MULTIPLICACION" || op.operador == "*") {
                            op.num1 * op.num2
                        } else if (op.operador.uppercase() == "DIVISION" || op.operador == "/") {
                            if (op.num2 > 0) op.num1 / op.num2 else 0
                        } else {
                            0
                        }

                        // Se obtiene el resultado, y es enviado en un String por el canal de salida
                        log.debug { "Resultado obtenido: $result" }

                        respuesta = result.toString()
                        salida.writeStringUtf8(respuesta)

                        log.debug { "Resultado enviado" }
                    }
                    /*else -> {
                        log.debug { "Tipo no identificado" }
                    }*/
                }
            }
        }
        // Terminamos la corrutina y cerramos lo necesario.
        procesarOperacion.join()

        log.debug { "Cerrando conexion" }
        withContext(Dispatchers.IO) {
            salida.close()
            socket.close()
        }
    }
}