/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FlingDownApp() {
    val density = LocalDensity.current
    val flingableRadius = 32.dp
    val flingableRadiusPx = with(density) { flingableRadius.toPx() }
    val explodeVelocityMagnitude = 1920.dp
    val explodeVelocityMagnitudePx = with(density) { explodeVelocityMagnitude.toPx() }

    val colors = MaterialTheme.colors
    val flingables = remember {
        List(10) {
            Flingable(
                radiusPx = flingableRadiusPx,
                color = if (it % 2 == 0) colors.secondary else colors.secondaryVariant,
            )
        }
    }
    val scope = rememberCoroutineScope()
    var count by remember { mutableStateOf(0) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    fun Flingable.onHit() {
        ++count
        isActive.value = false
    }

    val counterPosition by remember { derivedStateOf { size.offset() / 2f } }
    val counterRadius by animateDpAsState(
        targetValue = 64.dp + 8.dp * count,
        animationSpec = spring(
            dampingRatio = 0.4f,
            visibilityThreshold = Dp.VisibilityThreshold,
        ),
        finishedListener = { counterRadius ->
            val counterRadiusPx = with(density) { counterRadius.toPx() }
            flingables.filter { it.isActive.value }.forEach { flingable ->
                intersect(
                    flingable.position.value,
                    flingable.radiusPx,
                    counterPosition,
                    counterRadiusPx,
                )?.let {
                    flingable.onHit()
                }
            }
        },
    )
    val counterRadiusPx by remember { derivedStateOf { with(density) { counterRadius.toPx() } } }
    var countingDown by remember { mutableStateOf(false) }

    fun intersectCounter(position: Offset, radius: Float) =
        intersect(position, radius, counterPosition, counterRadiusPx)

    fun countDown() {
        if (countingDown) return
        countingDown = true
        scope.launch {
            while (count > 0) {
                delay(1000)
                --count
            }
            flingables.forEach { flingable ->
                launch {
                    explode(
                        flingable,
                        size,
                        counterPosition,
                        explodeVelocityMagnitudePx,
                        ::intersectCounter
                    )
                }
            }
            flingables.forEach { it.isActive.value = true }
            countingDown = false
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged {
                size = it
                scope.launch { flingables.initializePositions(counterPosition, counterRadiusPx) }
            }
            .pointerInput(Unit) {
                coroutineScope {
                    flingables.forEach { flingable ->
                        launch {
                            detectDragGestures(
                                flingable, size,
                                intersect = { position, radius ->
                                    intersectCounter(position, radius)?.also { flingable.onHit() }
                                }
                            )
                        }
                    }
                }
            }
            .drawBehind {
                flingables.forEach { if (it.isActive.value) draw(it) }
            },
        contentAlignment = Alignment.Center
    ) {
        val counterColor = MaterialTheme.colors.primary
        val mouthColor by animateColorAsState(if (countingDown) MaterialTheme.colors.primary else MaterialTheme.colors.primaryVariant)

        Eyes(counterRadius, counterColor)

        Surface(
            color = counterColor,
            shape = CircleShape,
        ) {
            Box(
                modifier = Modifier
                    .size(counterRadius * 2)
                    .clickable { countDown() },
                contentAlignment = Alignment.Center,
            ) {
                Mouth(counterRadius, mouthColor)
                Text(
                    text = if (count > 0) "$count" else "feed\nme",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.button.copy(fontSize = if (count > 0) 64.sp else 32.sp),
                )
                AnimatedVisibility(
                    visible = count > 0 && !countingDown,
                    enter = fadeIn(initialAlpha = 1f),
                    exit = fadeOut(),
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier
                            .padding(top = 92.dp)
                            .size(32.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Mouth(radius: Dp, color: Color) {
    Box(
        Modifier
            .size(radius * 2 - 16.dp)
            .background(color, CircleShape)
    )
}

@Composable
private fun Eyes(radius: Dp, color: Color) {
    val size = 64.dp
    val strokeWidth = 8.dp
    val pupilSize = size - strokeWidth * 4

    val infiniteTransition = rememberInfiniteTransition()
    val pupilWidth by infiniteTransition.animateValue(
        initialValue = pupilSize,
        targetValue = 0.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2500
                pupilSize at 2400 with FastOutLinearInEasing
            },
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        Modifier
            .size(size)
            .offset(-radius * 3f / 4, -radius * 3f / 4)
            .background(color, CircleShape)
    )
    Box(
        Modifier
            .size(size - strokeWidth * 2)
            .offset(-radius * 3f / 4, -radius * 3f / 4)
            .background(Color.White, CircleShape)
    )
    Box(
        Modifier
            .size(width = pupilWidth, height = pupilSize)
            .offset(-radius * 3f / 4, -radius * 3f / 4)
            .rotate(-45f)
            .background(Color.Black, CircleShape)
    )
    Box(
        Modifier
            .size(size)
            .offset(radius * 3f / 4, -radius * 3f / 4)
            .background(color, CircleShape)
    )
    Box(
        Modifier
            .size(size - strokeWidth * 2)
            .offset(radius * 3f / 4, -radius * 3f / 4)
            .background(Color.White, CircleShape)
    )
    Box(
        Modifier
            .size(width = pupilWidth, height = pupilSize)
            .offset(radius * 3f / 4, -radius * 3f / 4)
            .rotate(45f)
            .background(Color.Black, CircleShape)
    )
}

private fun DrawScope.draw(flingable: Flingable) = with(flingable) {
    drawCircle(color, radiusPx, position.value)
}

private fun IntSize.offset(): Offset = Offset(width.toFloat(), height.toFloat())

private suspend fun List<Flingable>.initializePositions(
    center: Offset,
    counterRadius: Float,
) = coroutineScope {
    forEachIndexed { index, flingable ->
        val radians = 2 * PI.toFloat() * index / size.toFloat()
        val direction = Offset(cos(radians), sin(radians))
        val radius = (counterRadius + 2 * flingable.radiusPx)
        launch {
            flingable.position.snapTo(center + direction * radius)
        }
    }
}
