JavaFieldtripBuffer
===================

Java implementation of the Fieldtrip realtime buffer.

Definition of the realtime buffer can be found [here](http://fieldtrip.fcdonders.nl/development/realtime).


Design
==================

Main Classes:

- **Buffer.java** contains main(),  initiates a DataModel and initiates a ServerThread for each incoming connection.
- **ServerThread.java** uses NetworkProtocol to decode/encode data
- **NetworkProtocol.java** contains a number of static functions that are implementations the network protocol defined [here](http://fieldtrip.fcdonders.nl/development/realtime/buffer_protocol).
- **DataModel.java** an interface which describes the input/output for the data storage.
	* **SimpleDataStore.java** an implementation of the DataModel which stores everything as simple lists.
	* **RingDataStore.java**  an implementation of the DataModel which stores everything in a circular buffer.

Network i/o container classes:

- **Message.java** container for the version, type and remaining bytes (in a ByteBuffer) of an incoming message.
- **Request.java** container for the begin and end sample/event for an GET\_EVT or GET\_DAT request.
- **WaitRequest.java** container for the number of samples/events and timeout for an WAIT\_DAT request.

Data container classes:

- **Data.java** container for the number of channels/samples, byte order, dataType and actual data (data is stored in bytes) used for GET\_DAT and PUT\_DAT requests.
- **Event.java** container for the event type/value type, event type/value size, sample, offset, duration, byteorder, value and type (value and type stored in bytes) used for the GET\_EVT and PUT\_EVT requests.
- **Header.java** container for the number of channels/samples/events, sampling frequency, datatype and chunks for a GET\_HDR and PUT\_HDR request.
- **Chunk.java** container for the type, size and data (data stored as bytes) used for the extended header in Header.java.
- **DataRingBuffer.java** a ring buffer that stores datapoints in a fixed size byte[][][] array.
- **EventRingBuffer.java** a ring buffer that stores events in a fixed size event[] array.

Custom Exceptions:

- **ClientException.java** an exception which is thrown when the client sends data that does not conform to the network protocol. If possible it will be caught and an appropriate error response is sent to the client, otherwise it will terminate the connection.
- **DataException.java** an exception wich is thrown when a problem occurs with the data. If possible it will be caught and an appropriate error response is sent to the client. 



Plan
==================

- [x] Implement Buffer, main program.
  - [x] Expand Buffer so it listens for connections and spawns a thread for each.
  - [x] Expand Buffer so it starts the DataStore.
  - [x] Handle optional arguments.
- [x] Implement ServerThread, class containing the per-connection logic.
  - [x] Implement basic listening loop.
  - [x] Create communication handling loop.
    - [x] Read message opening using NetworkProtocol.
	- [x] Handle put\_hdr and get\_hdr.
	- [x] Handle put\_dat and get\_dat.
	- [x] Handle put\_evt and get\_evt.
	- [x] Handle flush\_dat, flush\_evt and flush\_hdr.
	- [x] Handle wait_dat. 
- [x] Implement NetworkProtocol, class containing static functions which handle the protocol as defined [here](http://fieldtrip.fcdonders.nl/development/realtime/buffer_protocol).
  - [x] Implement message_def, which defines the standard opening of any message.
  - [x] Implement put\_hdr and get\_hdr, which handles header related communication.
  - [x] Implement put\_dat and get\_dat, which handles data related communication.
  - [x] Implement put\_evt and get\_evt, which handles event related communication.
  - [x] Implement flush\_dat, flush\_evt and flush\_hdr, which clears the data, events or header.
  - [x] Implement wait_dat, which is used to poll for new samples or events.
  - [x] Implement handling of extended hdr.
  - [x] Handle endianness.
- [x] Implement Message class, a simple container for a messages version, type and body.
- [x] Implement the DataStore, handles the actual data in memory and on disk.
  - [x] Write a simple datastore interface.
  - [x] Implement a simple array version.
    - [x] Implement read/write/flush header function.
    - [x] Implement read/write/flush data function.
    - [x] Implement read/write/flush event function.
  - [x] Expand to a circular buffer version.
- [x] Check for garbage collection optimizations. (Android garbage collection is concurrent.)
- [ ] Apply code optimizations.
	- [ ] Get baseline memory efficiency, using signalProxy and eegViewer.
	- [ ] Remove getters and setters.
	- [ ] Use **new** as little as possible.
		- [ ] Adapt data container classes so they are reusable.
		- [ ] Change code so data containers are reused as often as possible (probably need only a single data container of each type per thread).		
	- [ ] Check out memory efficiency of ByteBuffer and look for alternatives. (Like initiating a single big one per thread and reuse it.)
	- [ ] Check that **static** and **final** are used as often as possible.
	- [ ] Use enhanced for loops where possible. 
	- [ ] Check that each non-enhanced for loop stores the array length locally, as to prevent invocations of the size method.
- [ ] Check if handling of unsigned primitives works. (Gleaned from buffer.jar, but I'm pretty sure it is broken for sufficiently high values).
- [x] Check fancy wait fix. But buffer stall still occure when using eegviewer.

- [x] Testing
	- [x] Test in Python
		- [x] Test the put\_hdr and get\_hdr functionality.
		- [x] Test the put\_dat and get\_dat functionality.
		- [x] Test the put\_evt and get\_evt functionality.
		- [x] Test the wait\_dat functionality.
	- [x] Test in Java
		- [x] Test the put\_hdr, get\_hdr and flush\_hdr functionality.
		- [x] Test the put\_dat, get\_dat and flush\_dat functionality.
		- [x] Test the put\_evt, get\_evt and flush\_evt functionality.
		- [x] Test the wait\_dat functionality.
	- [x] Test Endianness using java client.
	- [x] Test using signal proxy.
	- [x] Test using EegViewer.
	- [x] Test using EventViewer.
	