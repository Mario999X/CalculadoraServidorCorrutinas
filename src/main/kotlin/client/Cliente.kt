package client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Operacion
import model.mensajes.Request
import mu.KotlinLogging

private val log = KotlinLogging.logger {}
private val json = Json

fun main() = runBlocking {

    // Indicamos el Dispatcher para el Cliente
    val selectorManager = SelectorManager(Dispatchers.IO)

    log.debug { "Por favor, introduzca su NOMBRE de USUARIO:" }
    val user = readln()
    log.debug { "Por favor, introduzca el PRIMER numero de su operacion:" }
    val num1 = readln().toInt()
    log.debug { "Por favor, introduzca el TIPO de OPERACION a realizar:" }
    val operador = readln()
    log.debug { "Por favor, introduzca el SEGUNDO numero de su operacion:" }
    val num2 = readln().toInt()

    val operacion = Operacion(user, num1, operador, num2)

    val socket = aSocket(selectorManager).tcp().connect("localhost", 6969)
    log.debug { "Conectado a ${socket.remoteAddress}" }

    val entrada = socket.openReadChannel()
    val salida = socket.openWriteChannel(true)

    // Lanzamos la corrutina que envia el objeto operacion

    val envioOperacion = launch {
        log.debug { "Lanzada corrutina de envio de operaciones" }

        // Dato a mandar, del tipo Request
        val request = Request(
            content = operacion,
            type = Request.Type.SEND
        )
        salida.writeStringUtf8(json.encodeToString(request) + "\n")
        log.debug { "$operacion enviada con exito, esperando solucion..." }
    }

    val reciboRespuesta = entrada.readUTF8Line()
    log.debug { "Resultado: $reciboRespuesta" }

    envioOperacion.join()

    log.debug { "Desconectando del servidor..." }
    withContext(Dispatchers.IO) {
        salida.close()
        socket.close()
    }

}