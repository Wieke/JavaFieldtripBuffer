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

Design
==================

- **Buffer.java** contains main(),  initiates a DataModel and initiates a ServerThread for each incoming connection.
- **ServerThread.java** uses NetworkProtocol to decode/encode data
- **NetworkProtocol.java** contains a number of static functions that are implementations the network protocol defined [here](http://fieldtrip.fcdonders.nl/development/realtime/buffer_protocol).
- **DataModel.java** an interface which describes the input/output for the data storage.
	* **SimpleDataStore.java** an implementation of the DataModel which stores everything as simple lists.
	* **BufferedDataStore.java** an implementation of the DataModel which stores everything in a circular buffer and periodically writes it's content to the hard disk.

- **Message.java** container for the version, type and remaining bytes (in a ByteBuffer) of an incoming message.
- **Request.java** container for the begin and end sample/event for an GET\_EVT or GET\_DAT request.
- **WaitRequest.java** container for the number of samples/events and timeout for an WAIT\_DAT request.
- **WaitResponse.java** container for the current number of samples/events for the response to a WAIT\_DAT request.

- **Data.java** container for the number of channels/samples, byte order, dataType and actual data (data is stored in bytes) used for GET\_DAT and PUT\_DAT requests.
- **Event.java** container for the event type/value type, event type/value size, sample, offset, duration, byteorder, value and type (value and type stored in bytes) used for the GET\_EVT and PUT\_EVT requests.
- **Header.java** container for the number of channels/samples/events, sampling frequency, datatype and chunks for a GET\_HDR and PUT\_HDR request.
- **Chunk.java** container for the type, size and data (data stored as bytes) used for the extended header in Header.java.

- **ClientException.java** an exception which is thrown when the client sends data that does not conform to the network protocol. If possible it will be caught and an appropriate error response is sent to the client, otherwise it will terminate the connection.
- **DataException.java** an exception wich is thrown when a problem occurs with the data. If possible it will be caught and an appropriate error response is sent to the client. 



Plan
==================

- [x] Implement Buffer, main program.
  - [x] Expand Buffer so it listens for connections and spawns a thread for each.
  - [x] Expand Buffer so it starts the DataStore.
  - [ ] Handle optional arguments.
- [ ] Implement ServerThread, class containing the per-connection logic.
  - [x] Implement basic listening loop.
  - [ ] Create communication handling loop.
    - [x] Read message opening using NetworkProtocol.
	- [x] Handle put\_hdr and get\_hdr.
	- [x] Handle put\_dat and get\_dat.
	- [ ] Handle put\_evt and get\_evt.
	- [x] Handle flush\_dat, flush\_evt and flush\_hdr.
	- [ ] Handle wait_dat. 
- [ ] Implement NetworkProtocol, class containing static functions which handle the protocol as defined [here](http://fieldtrip.fcdonders.nl/development/realtime/buffer_protocol).
  - [x] Implement message_def, which defines the standard opening of any message.
  - [x] Implement put\_hdr and get\_hdr, which handles header related communication.
  - [x] Implement put\_dat and get\_dat, which handles data related communication.
  - [x] Implement put\_evt and get\_evt, which handles event related communication.
  - [x] Implement flush\_dat, flush\_evt and flush\_hdr, which clears the data, events or header.
  - [x] Implement wait_dat, which is used to poll for new samples or events.
  - [ ] Implement handling of extended hdr.
  - [x] Handle endianness.
- [x] Implement Message class, a simple container for a messages version, type and body.
- [ ] Implement the DataStore, handles the actual data in memory and on disk.
  - [x] Write a simple datastore interface.
  - [ ] Implement a simple array version.
    - [ ] Implement read/write header function.
    - [ ] Implement read/write data function.
    - [ ] Implement read/write event function.
  - [ ] Expand to a circular buffer version.
  - [ ] Add periodic write to disk functionality.
  - [ ] Split write to disk functionality off in its own thread.
- [ ] Check for garbage collection optimizations.
- [ ] Check if handling of unsigned primitives works. (Gleaned from buffer.jar, but I'm pretty sure it is broken for sufficiently high values).


