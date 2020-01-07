package mx.softel.cirwireless.extensions

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Ejecuta un toast en cualquier contexto (en general Activity's)
 *
 * @param message Mensaje a mostrar (Recurso o texto)
 * @param duration Duración en milisegundos
 */
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).apply {
        show()
    }
}

fun Context.toast(message: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).apply {
        show()
    }
}

/**
 * Ejecuta un toast en cualquier clase que herede de [Fragment]
 *
 * @param message Mensaje a mostrar (Recurso o texto)
 * @param duration Duración en milisegundos
 */
fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(activity, message, duration).apply {
        show()
    }
}

fun Fragment.toast(message: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(activity, message, duration).apply {
        show()
    }
}