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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun FlingDownApp() {
    val density = LocalDensity.current
    val flingableRadius = 32.dp
    val flingableRadiusPx = with(density) { flingableRadius.toPx() }
    val explodeVelocityMagnitude = 1920.dp
    val explodeVelocityMagnitudePx = with(density) { explodeVelocityMagnitude.toPx() }

    val flingables = remember { List(10) { Flingable(radiusPx = flingableRadiusPx) } }
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

    fun intersectCounter(position: Offset, radius: Float) =
        intersect(position, radius, counterPosition, counterRadiusPx)

    fun reset() {
        count = 0
        flingables.forEach { flingable ->
            scope.launch {
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
    }

    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
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
        Surface(
            color = Color.Black,
            contentColor = Color.White,
            shape = CircleShape,
        ) {
            Box(
                modifier = Modifier
                    .size(counterRadius * 2)
                    .clickable { reset() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$count",
                    color = Color.White,
                    style = MaterialTheme.typography.button.copy(fontSize = 32.sp),
                )
            }
        }
    }
}

private fun DrawScope.draw(flingable: Flingable) = with(flingable) {
    drawCircle(color, radiusPx, position.value)
}

private fun IntSize.offset(): Offset = Offset(width.toFloat(), height.toFloat())
