---
layout: project
title: Hoptoad Notifer for Android
tagline: Automatically Notify Hoptoad of Exceptions in your Android App
version: 1.0
github_url: https://github.com/loopj/hoptoad-android
download_url: https://github.com/loopj/hoptoad-android/zipball/hoptoad-android-1.0
---


Overview
--------
The Hoptoad notifier for Android is designed to give you instant notification
of exceptions thrown from your Android applications. The notifier hooks into
`Thread.UncaughtExceptionHandler`, which means any uncaught exceptions will
trigger a notification to be sent to your Hoptoad project.


Features
--------
- Automatic notification of uncaught exceptions
- Exceptions are buffered to disk if there is no internet connection
  available, and sent later
- Designed to be robust, the notifier should not itself cause crashes or
  freezes
- Minimal cpu and memory footprint


Installation & Setup
--------------------
Download the latest .jar file from github and place it in your Android app's
`lib/` folder.

Import the HoptoadNotifier class in your app's main Activity.

{% highlight java %}
import com.loopj.android.hoptoad.HoptoadNotifier;
{% endhighlight %}

In your activity's `onCreate` function, register to begin capturing exceptions:
{% highlight java %}
HoptoadNotifier.register(this, "your-api-key-goes-here");
{% endhighlight %}


Configuration
-------------
TODO


Reporting Bugs or Feature Requests
----------------------------------
Please report any bugs or feature requests on the github issues page for this
project here:

<https://github.com/loopj/hoptoad-android/issues>


License
-------
TODO
