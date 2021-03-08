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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Adapted from the [FlingGame](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/animation/animation/integration-tests/animation-demos/src/main/java/androidx/compose/animation/demos/FlingGame.kt;drc=693e3ccfea4b69cf46676bd85dc4d314d8e62282) sample.
 */
suspend fun PointerInputScope.detectDragGestures(
    flingable: Flingable,
    size: IntSize,
    hitTest: (Offset) -> Intersection?,
) = coroutineScope {
    val radius = flingable.radiusPx
    val position = flingable.position
    while (true) {
        val pointerId = awaitPointerEventScope {
            var change: PointerInputChange
            do {
                change = awaitDown()
            } while ((change.position - position.value).getDistance() > radius || !flingable.isActive.value)
            change.consumeDownChange()
            launch { position.stop() }
            change.id
        }
        val velocityTracker = VelocityTracker()
        awaitPointerEventScope {
            dragPointer(pointerId) {
                launch { position.snapTo(position.value + it.positionChange()) }
                velocityTracker.addPosition(
                    it.uptimeMillis,
                    it.position
                )
            }
        }
        val (x, y) = velocityTracker.calculateVelocity()

        var endDueToHit = false
        fun Animatable<Offset, AnimationVector2D>.onEachFrame() {
            if (endDueToHit) return
            val intersection = hitTest(value) ?: return
            endDueToHit = true
            val (normal, depth) = intersection
            val targetValue = value + normal * depth + normal * EPSILON
            launch { snapTo(targetValue) }
        }

        launch {
            try {
                bounceAround(flingable, size, initialVelocity = Offset(x, y)) { onEachFrame() }
            } catch (_: CancellationException) {
            }
        }
    }
}

/**
 * Unlike [awaitFirstDown], returns the change for any (first, second, etc pointer) down event.
 */
private suspend fun AwaitPointerEventScope.awaitDown(
    requireUnconsumed: Boolean = true,
): PointerInputChange {
    var event: PointerEvent
    var index: Int
    do {
        event = awaitPointerEvent()
        index = event.changes.indexOfFirst {
            if (requireUnconsumed) it.changedToDown() else it.changedToDownIgnoreConsumed()
        }
    } while (index == -1)
    return event.changes[index]
}

/**
 * Unlike [drag], completes if the specified [pointerId] is raised.
 */
private suspend fun AwaitPointerEventScope.dragPointer(
    pointerId: PointerId,
    onDrag: (PointerInputChange) -> Unit,
): Boolean {
    while (true) {
        val change = awaitDragOrCancellation(pointerId) ?: return false

        if (change.changedToUpIgnoreConsumed()) {
            return true
        }

        // complete if the drag's active pointer has changed
        if (change.id != pointerId) {
            return true
        }

        onDrag(change)
    }
}
