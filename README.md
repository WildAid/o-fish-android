# WildAid O-FISH (Android App)

The [WildAid Marine Program](https://marine.wildaid.org/) work to protect vulnerable marine environments.

O-FISH (Officer Fishery Information Sharing Hub) is a multi-platform application that enables officers to browse and record boarding report data from their mobile devices.

This repo implements the Android O-FISH app.

Details on installing all applications making up the solution can be found [here](http://wildaid.github.io/).

## Prerequisites

This is the Android Mobile app for O-FISH. To build and use the app, you must first create and configure your serverless backend application using the [WildAid O-FISH MongoDB Realm repo](https://github.com/WildAid/o-fish-realm)

## Building and running the app

Build and run the app using Android Studio - but before building, set these values in the project's `local.properties` file:

```
realm_app_id=your_app_id # Find this from the [MongoDB Realm UI](https://realm.mongodb.com)

# Temporary
realm_partition=your_partition # Set this partition up in the backend Realm app
```

## Internal - Remove before making public
To speed up development and testing, you can also specify a default email-address/password pair to make logging in quicker by adding this to `local.properties`. **Not to be used in production!**:

```
realm_url=https://realm-dev.mongodb.com # Needed while still in cloud-qa

realm_user=your_user  # email address configured in the backend
realm_password=your_password
```
