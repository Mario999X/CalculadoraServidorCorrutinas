package model

import kotlinx.serialization.Serializable
@Serializable
data class Operacion(
    val user: String,
    val num1: Int,
    val operador: String,
    val num2: Int
){

    override fun toString(): String {
        return "$user: $num1 $operador $num2"
    }
}