# WildAid O-FISH Android App

The [WildAid Marine Program](https://marine.wildaid.org/) works to protect vulnerable marine environments.

O-FISH (Officer Fishery Information Sharing Hub) is a multi-platform application that enables officers to browse and record boarding report data from their mobile devices.

<BR><BR>Developers are expected to follow the <A HREF="https://www.mongodb.com/community-code-of-conduct">MongoDB Community Code of Conduct</A> guidelines.

This repo implements the Android O-FISH app.

## Prerequisites

This is the Android Mobile app for O-FISH. To build and use the app, you must [use the sandbox realm-app-id](https://wildaid.github.io/contribute/sandbox.html) or [build your own foundation](http://wildaid.github.io/build).

## Building and running the app

1. Open the code in Android studio.

1. Wait for the Gradle sync to finish.<BR>
1. Add the Realm App ID from your template to `local.properties`:<BR>
`realm_app_id=your_app_id`<BR>

To be able to login to the app once it is built, you need to create a user in your instance of the [O-FISH Realm App](https://github.com/WildAid/o-fish-realm) or [in the sandbox environment](https://wildaid.github.io/contribute/sandbox.html).

