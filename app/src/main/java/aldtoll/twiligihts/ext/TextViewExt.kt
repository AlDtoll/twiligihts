package aldtoll.twiligihts.ext

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView

fun TextView.addChangeAnimation() {
    val textView = this
    val shakeX =
        ObjectAnimator.ofFloat(textView, View.TRANSLATION_X, 0f, -10f, 10f, -10f, 0f)
    shakeX.duration = 500
    shakeX.interpolator = AccelerateDecelerateInterpolator()

    // Create the color change animation
    val colorAnim =
        ObjectAnimator.ofArgb(textView, "textColor", textView.currentTextColor, Color.RED)
    colorAnim.duration = 300
    colorAnim.repeatCount = 1
    colorAnim.repeatMode = ObjectAnimator.REVERSE

    // Create the animator set to play both animations
    val animatorSet = AnimatorSet()
    animatorSet.playTogether(shakeX, colorAnim)
    var oldText = textView.text
    textView.addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s.toString() != oldText.toString()) {
                animatorSet.start()
                oldText = s
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun afterTextChanged(s: Editable?) {

        }
    })
}