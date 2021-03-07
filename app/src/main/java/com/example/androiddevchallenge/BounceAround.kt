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
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

suspend fun bounceAround(
    flingable: Flingable,
    size: IntSize,
    initialVelocity: Offset,
    block: (Animatable<Offset, AnimationVector2D>.() -> Unit)? = null,
) {
    val radius = flingable.radiusPx
    val position = flingable.position

    position.updateBounds(
        Offset(radius, radius),
        Offset(size.width.toFloat() - radius, size.height.toFloat() - radius)
    )

    var startVelocity = initialVelocity
    do {
        val result =
            position.animateDecay(startVelocity, exponentialDecay(), block)
        startVelocity = result.endState.velocity

        with(position) {
            if (value.x == upperBound?.x || value.x == lowerBound?.x) {
                // x dimension hits bounds
                startVelocity = startVelocity.copy(x = -startVelocity.x)
            }
            if (value.y == upperBound?.y || value.y == lowerBound?.y) {
                // y dimension hits bounds
                startVelocity = startVelocity.copy(y = -startVelocity.y)
            }
        }
    } while (result.endReason == AnimationEndReason.BoundReached)
}
