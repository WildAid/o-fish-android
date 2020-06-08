# WildAid O-FISH (Android App)

** This app is work-in-progress **

The [WildAid Marine Program](https://marine.wildaid.org/) work to protect vulnerable marine environments.

O-FISH (Officer Fishery Information Sharing Hub) is a multi-platform application that enables officers to browse and record boarding report data from their mobile devices.

This repo implements the Android O-FISH app.

Details on installing all applications making up the solution can be found [here](http://wildaid.github.io/).

## Prerequisites

This is the Android Mobile app for O-FISH. To build and use the app, you must first create and configure your serverless backend application using the [WildAid O-FISH MongoDB Realm repo](https://github.com/WildAid/o-fish-realm)

## Building and running the app

Build and run the app using Android Studio - but before building, set these values in the project's `local.properties` file:

```
# Find this from the [MongoDB Realm UI](https://realm.mongodb.com)
realm_app_id=your_app_id

# Temporary
# Should match the `agency` attribute in any sample report documents in the Realm app/Atlas cluster
realm_partition=WildAid
realm_url=https://realm.mongodb.com
```
