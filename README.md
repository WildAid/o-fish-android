# WildAid O-FISH Android App

The [WildAid Marine Program](https://marine.wildaid.org/) works to protect vulnerable marine environments.

O-FISH (Officer Fishery Information Sharing Hub) is a multi-platform application that enables officers to browse and record boarding report data from their mobile devices.

This repo implements the Android O-FISH app.

Details on installing all applications making up the solution can be found [here](http://wildaid.github.io/).

## Prerequisites

This is the Android Mobile app for O-FISH. To build and use the app, you must first create and configure your serverless backend application using the [WildAid O-FISH MongoDB Realm repo](https://github.com/WildAid/o-fish-realm).

## Building and running the app

1. Open the code in Android studio.

1. Wait for the Gradle sync to finish.<BR>
1. Add the Realm App ID from your template to `local.properties`:<BR>
`realm_app_id=your_app_id`<BR>

1. <A HREF="https://developer.android.com/studio/run">Build and run as normal with Android Studio</A>, either directly to your physical or virtual device.<BR><BR>

