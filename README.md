JavaFieldtripBuffer
===================

Java implementation of the Fieldtrip realtime buffer.

Definition of the realtime buffer can be found [here](http://fieldtrip.fcdonders.nl/development/realtime).

A java fieldtrip buffer client was already made and is availeble over [here](https://github.com/jadref/buffer_bci), it may contain some useful classes:

*  DataType: for converting bytestrings to java types.
*  WrappedObject: wrapper for bytestrings (includes the string, size, type, numel)
*  Header: incomplete. Wrapper around the buffer header, includes serialization and such.
*  DataDescription: Class describing sample block properties.
*  BufferEvent: Class for buffer event, includes serialization.
*  BufferClient: contains use of socket stuff.

The client stores data as short[][] arrays.

Plan
==================

- [x] Implement Buffer, main program.
  - [x] Expand Buffer so it listens for connections and spawns a thread for each.
  - [x] Expand Buffer so it starts the DataStore.
  - [ ] Handle optinal arguments.
- [ ] Implement ServerThread, class containing the per-connection logic.
  - [x] Implement basic listening loop.
  - [ ] Create communication handling loop.
    - [x] Read message opening using NetworkProtocol.
    - [ ] Grab appropriate message details using NetworkProtocol.
    - [ ] Read or write required data using DataStore.
    - [ ] Sent appropriate response using NetworkProtocol. 
- [ ] Implement NetworkProtocol, class containing static functions which handle the protocol as defined [here](http://fieldtrip.fcdonders.nl/development/realtime/buffer_protocol).
  - [x] Implement message_def, which defines the standard opening of any message.
  - [x] Implement put\_hdr and get\_hdr, which handles header related communication.
  - [ ] Implement put\_dat and get\_dat, which handles data related communication.
  - [ ] Implement put\_evt and get\_evt, which handles event related communication.
  - [ ] Implement flush\_dat, flush\_evt and flush\_hdr, which clears the data, events or header.
  - [ ] Implement wait_dat, which is used to poll for new samples or events.
  - [ ] Implement handling of extended hdr.
  - [x] Handle endianness.
- [x] Implement Message class, a simple container for a messages version, type and body.
- [ ] Implement the DataStore, handles the actual data in memory and on disk.
  - [x] Write a simple datastore interface.
  - [ ] Implementa simple array version.
    - [ ] Implement read/write header function.
    - [ ] Implement read/write data function.
    - [ ] Implement read/write event function.
  - [ ] Expand to a circular buffer version.
  - [ ] Add periodic write to disk functionality.
  - [ ] Split write to disk functionality off in its own thread.
- [ ] Check for garbage collecion optimizations.
- [ ] Check if handling of unsigned primites works.


