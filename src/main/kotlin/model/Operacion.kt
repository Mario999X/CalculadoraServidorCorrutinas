package model

import java.io.Serializable

data class Operacion(
    val user: String,
    val num1: Int,
    val operador: String,
    val num2: Int
) : Serializable {

    override fun toString(): String {
        return "$user: $num1 $operador $num2"
    }
}