# Remote Access
A remote desktop/admin application.

## How to run
Compiling from project directory: `javac -d bin src/*.java`

Launch arguments:
* `-client`
* `-remote`
* `-ip <IP address>` - Only necessary for the client
* `-p <Port>` - The specified port + 1 will be used for file transfers

Default args:
* `-ip 127.0.0.1`
* `-p 7777` - 7778 will be used for file transfers

From project directory
* On remote machine: `java -cp bin Main -remote`
* On client/terminal machine: `java -cp bin Main -client`

From src directory
* On remote machine: `java Main -remote`
* On client/terminal machine: `java Main -client`


## Current functionality
* Screen sharing
    * Screen captures of the remote machine are sent to the client whenever requested by the client.
    * Screen captures are resized on the remote machine to the size specified in the request before being sent. The original aspect ratio is maintained if only a width is specified.

* Remote user input
    * The clients JFrame holds an InputReader instance, responsible for listening to user mouse (click and scroll) and keyboard input. Inputs are sent to the remote machine.
    * The remote machine uses a Robot instance, which actions the received user inputs.
    * The position of mouse clicks are corrected on the remote machine, due to the discrepancy between the clients view and the actual screen resolution on the remote machine.
    * If the client and remote machine are found to be on different operating systems, some inputs are converted to the equivelant keys before being sent.

* File sharing
    * The client can initiate a file transfer, which runs on another thread with its own socket to not interrupt the rest of the client-remote communication during transfer.
    * The port used for file transfer is the specified port on launch + 1.
    * Currently only the client can send a file to the remote, where it is written to the directory in which the program was launched from.
    * The native file browser/explorer is used to select which file to send.

## Planned functionality
* Improve image resizing/scaling text readability while also maintaining a fast speed (ImageIO is too slow).
* Have the client initiate a file transfer to receive a file from the remote machine.
* The ability for the client and remote machine to be able to switch roles.
* Eventually a GUI to be able to select role (client or remote) and enter connection information such as IP address and port.