# Useful Cross-Dimensional Radios
Radio communication is very useful in real life, but perhaps not so much in minecraft,
this mod was built (mainly as a library mod for a mod that hasn't come out yet) to make radios
make sense in a minecraft context

# The Radio Specification
A radio is a device that is used to transmit and receive signals

In our case it has specific functions:
- to receive communication from one of several "bands"
- received communication could be News, Distress or radio anomalies
- to transmit communication on these "bands"
- transmitted communication could be requests to stations or ships
- all information is transmitted as "spoken" text

Bands are explained as such:
- There are 100 bands of communication
- lower bands require less power to transmit and less complex machinery to receive
- higher bands can be received from a lot further away
- band 1 can be received 8 blocks away, and each successive band multiplies this distance by 8
- every quarter of the max distance, information becomes fainter/decays
- distances are calculated across dimensions (when the dimension has a registered physical location)

Radios could be used for:
- some structures could have receivers that you can transmit to, and will respond with information
  - research stations or airfields may use a low band responder to give atmospheric conditions
- some responders may instead be on higher bands and send their location
- communication may be encrypted with a passphrase
- anyone who has that passphrase stored will be able to see the information
- a player could try and guess the passphrase, some machines may also be able to do this

In most circumstances, `transmit` should only be called once for each message, and then again to stop the transmission
`receive` should be polled when necessary, however a receiver can choose to subscribe to a transmitter if it is a block

"Subscriber Radios" are identified by the `ISubscriberRadio` interface, they immediately perform an action when
they are triggered, these must be used in a blocks class.

"Radio Actions" are called when a transmitter sends a message, they are meant to be used to activate radios
that exist in structures that couldn't have been registered by a player - they must be added to ACTIONS every time the game starts.