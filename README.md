# Remote Access
A remote desktop/admin application.

## How to run
Compiling from project directory: `javac -d bin src/*.java`

Laucnh arguments:
* `-client`
* `-remote`
* `-ip <IP address>` Only for client
* `-p <Port>`

Default args:
* `-ip 127.0.0.1`
* `-p 7777`

From project directory
* On remote machine: `java -cp bin Main -remote`
* On client/terminal machine: `java -cp bin Main -client`

From src directory
* On remote machine: `java Main -remote`
* On client/terminal machine: `java Main -client`

## Planned features
- Live stream of remote machine screen to client.
    - resizing/scaling of image to suit client display size.
- Transmission of client key presses and mouse clicks, to be actioned/entered on remote machine.
- Transfer of files between machines.