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
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

class Flingable(
    val radiusPx: Float,
    val position: Animatable<Offset, AnimationVector2D>,
    val color: Color,
    val isActive: MutableState<Boolean>,
)

fun Flingable(
    radiusPx: Float,
    startPosition: Offset = Offset(radiusPx, radiusPx),
    color: Color = Color(
        red = Random.nextFloat(),
        green = Random.nextFloat(),
        blue = Random.nextFloat(),
    ),
): Flingable = Flingable(
    radiusPx = radiusPx,
    position = Animatable(startPosition, Offset.VectorConverter),
    color = color,
    isActive = mutableStateOf(true),
)
