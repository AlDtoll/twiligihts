package aldtoll.twiligihts.ext

import android.content.res.Resources
import android.view.View

val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun View.setOnClickScaleAnimation(
    scaleDown: Float = 0.9f,
    scaleUp: Float = 1f,
    duration: Long = 150,
    onClick: (View) -> Unit
) {
    setOnClickListener { view ->
        // Отменяем предыдущие анимации
        view.animate().cancel()
        view.scaleX = 1f
        view.scaleY = 1f
        // Вызываем переданный колбэк
        onClick(view)
        // Анимация сжатия
        view.animate()
            .scaleX(scaleDown)
            .scaleY(scaleDown)
            .setDuration(duration)
            .withEndAction {
                // Возвращаем к исходному размеру
                view.animate()
                    .scaleX(scaleUp)
                    .scaleY(scaleUp)
                    .setDuration(duration)
                    .start()


            }
            .start()
    }
}