ProgressProfileView
===================
[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0) [![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)

Android custom view to load an avatar or profile image with a progress indicator.

It works on Marshmallow.

![Portrait][1]

**You can also Preview the result on Android Studio!**

![Android Studio Preview][2]

Background
----------
**ProgressProfileView** extends ImageView, so you can set xml attributes as usual.
<br>It will be square measured both on landscape and portrait.

***You don't need to transform resources placeholders or loaded images from Picasso or Glide to rounded images!***

**ProgressProfileView** will clip proportionally all images loaded.
<br>Just remember to set `android:scaleType="centerCrop"` on the component.

How to:
------

If you are working with gradle, add the dependency to your build.gradle file:
```groovy
dependencies{
    compile 'com.kuassivi.view:progressprofile:1.0.3'
}
```
If you are working with maven, do it into your pom.xml
```xml
<dependency>
    <groupId>com.kuassivi.view</groupId>
    <artifactId>progressprofile</artifactId>
    <version>1.0.3</version>
    <type>aar</type>
</dependency>
```

Add the **ProgressProfileView** component in some place on the layout.
<br>Set your prefered ImageView attributes on it.

```xml
<com.kuassivi.view.ProgressProfileView
        android:id="@+id/profile"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:scaleType="centerCrop"
        android:adjustViewBounds="true"
        android:src="@drawable/my_avatar"
        app:progress="42"
        app:progressRingSize="10dp"
        app:progressRingCap="ROUND"/>
```

>Get the object on your activity or fragment.

```java 
ProgressProfileView profile = (ProgressProfileView) findViewById(R.id.profile);
```

>Set your desired progress.

```java 
profile.setProgress(59.5f);
```

>To animate the ring, call `#startAnimation()`

```java 
profile.startAnimation();
```

>If you need to handle the progress animation update or listen to the end of the animation, 
just get the `ObjectAnimator` calling the `getAnimator()` method from your **ProgressProfileView** object.

```java 
profile.getAnimator().addListener(new AnimatorListener());
```
```java 
profile.getAnimator().addUpdateListener(new AnimatorUpdateListener());
```
```java 
profile.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
```

>If you are loading images from Glide, consider to implement a custom ViewTarget like this one
```java
// Using Glide as usual
Glide.with(this)
    .load("http://your/server/path")
    .placeholder(R.drawable.ic_icon_user_default)
    .fitCenter() // Fit and center the bitmap
    .into(profile);
```

Features:
---------

 *     `app:max="100"` - Max value for progress indicator
 *     `app:progress="50"` - Current progress value
 *     `app:backgroundRingSize="20dp"` - Set the size of the background ring (not set, means use the same as the <i>progressRingSize</i>)
 *     `app:progressRingSize="20dp"` - Set the size of the progress ring
 *     `app:backgroundRingColor="@color/my_color"` - Set the color of the background ring (it can be an hex color as well)
 *     `app:progressRingColor="@color/my_color"` - Set the color of the progress ring (it can be an hex color as well)
 *     `app:progressRingCap="BUTT"` - Set the cap style of the progress ring (Possible values: BUTT, ROUND, SQUARE)
 
License
-------

Copyright 2015 Francisco Gonzalez-Armijo Riádigos

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
[2]: ./art/android-studio-preview.png
