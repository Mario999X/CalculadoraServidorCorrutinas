package client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
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

    // Generamos el objeto Operacion pregutando por teclado
    log.debug { "Por favor, introduzca su NOMBRE de USUARIO:" }
    val user = readln()
    log.debug { "Por favor, introduzca el PRIMER numero de su operacion:" }
    val num1 = readln().toInt()
    log.debug { "Por favor, introduzca el TIPO de OPERACION a realizar:" }
    val operador = readln()
    log.debug { "Por favor, introduzca el SEGUNDO numero de su operacion:" }
    val num2 = readln().toInt()

    // Objeto a enviar
    val operacion = Operacion(user, num1, operador, num2)

    // Conectamos con el servidor
    val socket = aSocket(selectorManager).tcp().connect("localhost", 6969)
    log.debug { "Conectado a ${socket.remoteAddress}" }

    // Preparamos los canales de lectura-escritura
    val entrada = socket.openReadChannel()
    val salida = socket.openWriteChannel(true)

    // Esperamos recibir el historial por parte del servidor
    log.debug { "Esperando el historial..." }
    val responseHistorial = entrada.readUTF8Line()
    //println(responseHistorial)

    val historial = json.decodeFromString<List<Operacion>>(responseHistorial!!)
    log.debug { "Historial: $historial" }

    // Lanzamos la corrutina que envia el objeto operacion
    val envioOperacion = launch {
        log.debug { "Lanzada corrutina de envio de operaciones" }

        // Dato a mandar, del tipo Request, elegimos el tipo de mensaje y el contenido
        val request = Request(
            content = operacion,
            type = Request.Type.SEND
        )
        // Lo preparamos, y lo mandamos como un json
        salida.writeStringUtf8(json.encodeToString(request) + "\n") // Añadimos el salto de línea para que se envíe
        log.debug { "$operacion enviada con exito, esperando solucion..." }
    }

    // Esperamos a la respuesta del servidor, en este caso devuelve la solucion como un string
    val reciboRespuesta = entrada.readUTF8Line()
    log.debug { "Resultado: $reciboRespuesta" }

    // Recogemos la corrutina
    envioOperacion.join()

    // Cerramos la salida y el propio socket
    log.debug { "Desconectando del servidor..." }
    withContext(Dispatchers.IO) {
        salida.close()
        socket.close()
    }

}