# Flingdown

![Workflow result](https://github.com/justasm/compose-flingdown/workflows/Check/badge.svg)

## :scroll: Description
A toy countdown timer built to demonstrate Jetpack Compose UI. Fling around the pellets using
multi-touch gestures to feed the little creature.

## :bulb: Motivation and Context
The app is a playground for several experiments with Jetpack Compose.
- [Custom multi-touch gestures.][gestures]
- Animation ([high-level][anim-high-level], [animate*AsState][anim-as-state], [Animatable][anim-animatable], [infinite][anim-infinite]).
- [Custom fonts.][font]

## :camera_flash: Screenshots
<img src="/results/screenshot_1.png" width="260">&emsp;<img src="/results/screenshot_2.png" width="260">

## License
```
Copyright 2020 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[gestures]: app/src/main/java/com/example/androiddevchallenge/Drag.kt#L94
[anim-high-level]: app/src/main/java/com/example/androiddevchallenge/FlingDownApp.kt#L201
[anim-as-state]: app/src/main/java/com/example/androiddevchallenge/FlingDownApp.kt#L104
[anim-animatable]: app/src/main/java/com/example/androiddevchallenge/BounceAround.kt#L42
[anim-infinite]: app/src/main/java/com/example/androiddevchallenge/FlingDownApp.kt#L234
[font]: app/src/main/java/com/example/androiddevchallenge/ui/theme/Type.kt#L24
