package monitor

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable
import model.Operacion
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger

private val log = KotlinLogging.logger { }

private const val LIMITE_HISTORIAL: Int = 3

@Serializable
object Cache {

    private var _operaciones =
        MutableSharedFlow<Operacion>(replay = LIMITE_HISTORIAL, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val operaciones: SharedFlow<Operacion> get() = _operaciones.asSharedFlow()

    private var _recibidos = AtomicInteger(0) // Nos permite evitar, junto al IF en el Gestor, que se trate de tomar.
    val size
        get() = if (_recibidos.get() > LIMITE_HISTORIAL) LIMITE_HISTORIAL else _recibidos.get()

    suspend fun add(operacion: Operacion) {
        log.debug { "$operacion agregada a cache" }
        _operaciones.emit(operacion)
        _recibidos.incrementAndGet()
    }
}