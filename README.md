# WildAid O-FISH Android App

The [WildAid Marine Program](https://marine.wildaid.org/) works to protect vulnerable marine environments.

O-FISH (Officer Fishery Information Sharing Hub) is a multi-platform application that enables officers to browse and record boarding report data from their mobile devices.

<BR><BR>Developers are expected to follow the <A HREF="https://www.mongodb.com/community-code-of-conduct">MongoDB Community Code of Conduct</A> guidelines.

This repo implements the Android O-FISH app.

The details behind the data architecture, schema, and partitioning strategy are described in [Realm Data and Partitioning Strategy Behind the WildAid O-FISH Mobile Apps](https://developer.mongodb.com/how-to/realm-data-architecture-ofish-app).

## Prerequisites

This is the Android Mobile app for O-FISH. To build and use the app, you must [use the sandbox realm-app-id](https://bit.ly/ofishsandbox) or [build your own foundation](http://wildaid.github.io/build).

## Building and running the app

1. Open the code in Android studio.

1. Wait for the Gradle sync to finish.<BR>
1. Add the Realm App ID from your template to `local.properties` AND `realm.properties` in the top-level directory (where this README is):<BR>
`realm_app_id=your_app_id`<BR>
e.g. for the sandbox:
`realm_app_id=wildaidsandbox-mxgfy`<BR>

To be able to login to the app once it is built, you need to create a user in your instance of the [O-FISH Realm App](https://github.com/WildAid/o-fish-realm) or [in the sandbox environment](https://bit.ly/ofishsandbox).

For example, if you are using the sandbox, your files should look like this:
`$ cat local.properties`
`realm_app_id=wildaidsandbox-mxgfy`

`$ cat realm.properties`
`realm_app_id=wildaidsandbox-mxgfy`
