Localization information for the o-fish-android project

Localization files are in app/src/main/res/values-CODE/strings.xml
e.g.
app/src/main/res/values-en-rUS/strings.xml
app/src/main/res/values-fr/strings.xml
app/src/main/res/values/strings.xml

Note the default is in app/src/main/res/values/strings.xml (no CODE)

The format is:
`<string name="variable_name">Text to Display</string>`

e.g.:
`<string name="remove_note">Remove Note</string>`

You will change the "Text to Display" part.

To create a new translation file, copy the default to a new directory. E.g. to add in Hindi, which has an ISO 2 letter language code of "hi":
`cd app/src/main/res`
`cp -pr values values-hi`

and then edit values-hi/strings.xml, replacing the English with Hindi translations.

If there is no translation needed, you can delete the line from your localization file. 
