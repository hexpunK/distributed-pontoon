# distributed-pontoon
## Introduction
This project is just the repository to keep my coursework for CMPCMC34: Distributed Computing - Assignment 1 manageable.

The basic idea of the project is to create a distributed system for playing a game of Pontoon (Blackjack, 21, etc.). The final application will contain a server that acts as the dealer, this server will be capable of playing multiple games simultaneously (yay threading!). The client side part of this application will contain both a human controllable client (hopefully with a GUI!) and a automated client. The automated clients will attempt to play multiple games of Pontoon simultaneously with multiple servers.

## Ideas Considered
Currently the project uses a custom messaging system, which might not be the greatest idea as it does require some boilerplate. Later versions will hopefully move to the Java RMI API because it would cut down a lot of work.
