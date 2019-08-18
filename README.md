# Fabric Server Administration Mod

## About
This mod is just something I've been working on in my free time. I <3 coding & Minecraft

This mod is a server-side mod for server administration, built for the Fabric loader. A sewing machine stitches fabric the same way this mod sews Minecraft chunk protection together, thus the name.

- Current versions require a MySQL database, but SQLite is planned for the future.
- Fabric API is also required

## In-game Guides
This mod adds the ability to hand out pre-written books via Sign interaction. Custom books can be implemented by adding to the "books.json" example in the repo's root directory. *This file is not generated by the mod, and must be added to your `/config/SewingMachine` folder*

If you have an IDE or Text Editor that supports the JSON Schema, there is also an included schema `books_schema.json`

## Server-side Translations
Minecraft supports translations via translation keys, however the server is unable to define new translation keys to the client ..so I improvised. The server keeps track of connected clients languages, and from that bit of information I was able to create language files that, when read, are sent as Literals to each player. Because of this issue, adding translations was an after though, and some messages may be missing from the translations files.

If you want to add a translation, submit a PR.