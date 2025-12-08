package aldtoll.twiligihts.ui.screen.game_screen.compose

import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.ui.screen.game_screen.adapter.HandsAdapter
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

@Composable
fun SwipeablePanel(
    modifier: Modifier = Modifier,
    hands: List<Hand> = emptyList(),
    handsLiveData: LiveData<ArrayList<Hand>>? = null,
    callback: HandsAdapter.Callback? = null
) {
    val density = LocalDensity.current
    var isExpanded by remember { mutableStateOf(false) }

    // Ширина панели
//    val panelWidthDp = 130.dp
    val panelWidthDp = 0.dp
    val panelWidthPx = with(density) { panelWidthDp.toPx() }

    // Сколько показывать изначально (30% видно, 70% скрыто)
    val peekWidthPx = panelWidthPx * 0.3f
    // Сколько скрыто изначально (70% от общей ширины)
    val hiddenWidthPx = panelWidthPx * 0.7f
    // Максимальный offset - это сколько нужно сдвинуть, чтобы показать полностью
    val maxOffsetPx = hiddenWidthPx

    Log.d(
        "HandsView",
        "Panel: width=${panelWidthDp}, peek=${peekWidthPx}px, hidden=${hiddenWidthPx}px, maxOffset=${maxOffsetPx}px"
    )

    // Анимация между двумя состояниями
    val animatedOffset by animateFloatAsState(
        targetValue = if (isExpanded) maxOffsetPx else 0f,
        animationSpec = tween(300),
        label = "panelAnimation"
    )

    val context = LocalContext.current

    // Наблюдаем за LiveData, если она передана
    val observedHands by handsLiveData?.observeAsState()
        ?: remember { mutableStateOf<ArrayList<Hand>?>(null) }
    val currentHands = observedHands ?: ArrayList(hands)

    // Логирование для отладки
    LaunchedEffect(currentHands) {
        Log.d(
            "HandsView",
            "Current hands updated: count=${currentHands.size}, hands=${currentHands.map { it.name }}"
        )
    }

    // Обертка для ограничения ширины
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(panelWidthDp) // Ограничиваем ширину контейнера
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(panelWidthDp) // Фиксированная ширина панели - не растягивается
                .offset {
                    // Начальное положение - частично скрыто слева
                    // При isExpanded = false: offset = 0, позиция = -hiddenWidthPx (видно только peek справа)
                    // При isExpanded = true: offset = maxOffsetPx, позиция = -hiddenWidthPx + maxOffsetPx = 0 (полностью видно)
                    val xOffset = (-hiddenWidthPx + animatedOffset).roundToInt()
                    Log.d(
                        "HandsView",
                        "Panel offset: x=$xOffset, animatedOffset=$animatedOffset, isExpanded=$isExpanded, panelWidth=${panelWidthDp}"
                    )
                    IntOffset(x = xOffset, y = 0)
                }
                .background(Color(0x80FF0000)) // Полупрозрачный красный
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    var startTime = 0L
                    var isDragging = false

                    // Обработка тапа
                    detectTapGestures(
                        onTap = {
                            if (!isDragging) {
                                Log.d("HandsView", "Panel tapped, toggling")
                                isExpanded = !isExpanded
                            }
                        }
                    )

                    // Обработка свайпа
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                            startTime = System.currentTimeMillis()
                            totalDrag = 0f
                            Log.d("HandsView", "Swipe started, isExpanded: $isExpanded")
                        },
                        onDragEnd = {
                            isDragging = false
                            val dragDuration = System.currentTimeMillis() - startTime
                            // Определяем направление свайпа
                            if (totalDrag > 20f) {
                                // Свайп вправо - открываем
                                Log.d("HandsView", "Swipe right, expanding")
                                isExpanded = true
                            } else if (totalDrag < -20f) {
                                // Свайп влево - закрываем до 30%
                                Log.d("HandsView", "Swipe left, collapsing")
                                isExpanded = false
                            }
                            // Если движение было маленьким, не меняем состояние
                            totalDrag = 0f
                        }
                    ) { change, dragAmount ->
                        totalDrag += dragAmount
                        Log.d(
                            "HandsView",
                            "Swipe drag: dragAmount=$dragAmount, totalDrag=$totalDrag"
                        )
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        Log.d(
                            "HandsView",
                            "Box size changed: width=${size.width}, height=${size.height}"
                        )
                    }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text(
                    text = "← СВЕРНУТЬ",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(8.dp)
                )
                // RecyclerView с фиксированными размерами
                var handsAdapter: HandsAdapter? by remember { mutableStateOf(null) }

                AndroidView(
                    factory = { ctx ->
                        RecyclerView(ctx).apply {
                            layoutManager = LinearLayoutManager(ctx)

                            // Важно: не фиксируем размер, чтобы RecyclerView мог правильно измеряться
                            setHasFixedSize(false)

                            // Устанавливаем clipToPadding для правильного отображения
                            clipToPadding = false

                            val adapter = if (callback != null) {
                                HandsAdapter.newInstance(callback, ctx, this)
                            } else {
                                HandsAdapter.newInstance(
                                    object : HandsAdapter.Callback {},
                                    ctx,
                                    this
                                )
                            }
                            this.adapter = adapter
                            handsAdapter = adapter

                            // Инициализируем данные сразу после создания
                            val initialHandsList =
                                ArrayList(currentHands.map { hand -> hand.copy() })
                            Log.d(
                                "HandsView",
                                "RecyclerView created, initializing with ${initialHandsList.size} hands"
                            )

                            adapter.updateData(initialHandsList)

                            // Принудительно запрашиваем layout после установки адаптера
                            post {
                                Log.d(
                                    "HandsView",
                                    "RecyclerView post: width=$width, height=$height, itemCount=${adapter.itemCount}"
                                )
                                if (width == 0 || height == 0) {
                                    Log.w(
                                        "HandsView",
                                        "RecyclerView has zero size! Requesting layout again..."
                                    )
                                    requestLayout()
                                }
                                invalidate()
                            }
                        }
                    },
                    modifier = Modifier
                        .width(130.dp)
                        .fillMaxHeight(),
                    update = { view ->
                        handsAdapter?.let { adapter ->
                            val handsList = ArrayList(currentHands.map { hand -> hand.copy() })
                            Log.d(
                                "HandsView",
                                "AndroidView update: updating RecyclerView with ${handsList.size} hands, view size: ${view.width}x${view.height}"
                            )
                            adapter.updateData(handsList)
                            // Принудительно обновляем layout после обновления данных
                            view.post {
                                Log.d(
                                    "HandsView",
                                    "RecyclerView post update: width=${view.width}, height=${view.height}, itemCount=${adapter.itemCount}"
                                )
                                view.requestLayout()
                                view.invalidate()
                            }
                        }
                    }
                )
            }
        }

        // Обновляем данные при изменении списка рук
        LaunchedEffect(currentHands) {
            // Обновление будет через update callback в AndroidView
        }
    }
}
