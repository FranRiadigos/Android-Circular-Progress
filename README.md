Circular Progress
===================
[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0) [![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)

![Portrait][1]&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;![Ring Samples][2]&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;![Gradient Samples][3]

**Preview in Android Studio**

![Android Studio Preview][4]

## Dependency

Latest stable version: 
[![Bintray Version](https://img.shields.io/bintray/v/kuassivi/maven/circular-progress.svg)](http://jcenter.bintray.com/com/kuassivi/maven/circular-progress/)

If you are working with gradle, add the dependency to your build.gradle file:
```groovy
dependencies{
    implementation "com.github.franriadigos:circular-progress:<latest version>"
}
```

How to use:
-----------

Add `ImageViewCircularProgress` or `FrameLayoutCircularProgress` in the layout.

```xml
<com.franriadigos.view.ImageViewCircularProgress
        android:id="@+id/view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:scaleType="centerCrop"
        android:adjustViewBounds="true"
        android:src="@drawable/my_placeholder_image"
        app:progress="42"
        app:progressRingSize="10dp"
        app:progressRingCorner="ROUND"
        app:progressRingOutline="true"/>
```

>Get the `CircularProgress` view in your activity or fragment.

```java 
ImageViewCircularProgress view = findViewById(R.id.view);
```

>Set your desired progress

```java 
view.setProgress(59.5f);
```

>Start the animation

```java 
view.startAnimation();
```

>You can access the `ObjectAnimator` by calling `getAnimator()`

```java 
profile.getAnimator().addListener(new AnimatorListener());
```

License
-------

    Copyright 2015-2020 Fran Riadigos
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[1]: ./art/portrait.gif
[2]: ./art/ring-samples.png
[3]: ./art/gradient-samples.png
[4]: ./art/android-studio-preview.png
