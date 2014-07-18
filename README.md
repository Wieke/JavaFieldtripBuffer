JavaFieldtripBuffer
===================

Java implementation of the Fieldtrip realtime buffer.

Definition of the realtime buffer can be found [here](http://fieldtrip.fcdonders.nl/development/realtime).

A jara fieldtrip buffer client was already made and is availeble in [here](https://github.com/jadref/buffer_bci), it may contain some useful classes:

*  DataType: for converting bytestrings to java types.
*  WrappedObject: wrapper for bytestrings (includes the string, size, type, numel)
*  Header: incomplete. Wrapper around the buffer header, includes serialization and such.
*  DataDescription: Class describing sample block properties.
*  BufferEvent: Class for buffer event, includes serialization.
*  BufferClient: contains use of socket stuff.

The client stores data as short[][] arrays.

Plan
==================


- [ ] Implement Buffer, main program.
  - [x] Expand Buffer so it listens for connections and spawns a thread for each.
  - [ ] Expand Buffer so it starts the DataStore.
- [ ] Implement ServerThread, class containing the per-connection logic.
  - [x] Implement basic listening loop.
  - [ ] Integrate with NetworkProtocol.
  - [ ] Consider turning NetworkProtocol in a ServerThread.
- [ ] Implement NetworkProtocol, abstract class which handles the protocol as defined [here](http://fieldtrip.fcdonders.nl/development/realtime/buffer_protocol).
  - [ ] Implement message_def, which defines the standard opening of any message.
  - [ ] Implement put\_hdr and get\_hdr, which handles header related communication.
  - [ ] Implement put\_dat and get\_dat, which handles data related communication.
  - [ ] Implement put\_evt and get\_evt, which handles event related communication.
  - [ ] Implement flush\_dat, flush\_evt and flush\_hdr, which clears the data, events or header.
  - [ ] Implement wait_dat, which is used to poll for new samples or events.
  - [ ] Implement handling of extended hdr.
- [ ] Implement the DataStore, handles the actual data in memory and on disk.
  - [ ] Implementa simple array version.
  - [ ] Expand to a circular buffer version.
  - [ ] Add periodic write to disk functionality.
  - [ ] Split write to disk functionality off in its own thread.
- [ ] Check for garbage colleciont optimizations.


