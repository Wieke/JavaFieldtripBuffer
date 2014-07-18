JavaFieldtripBuffer
===================

Java implementation of the Fieldtrip bealtime buffer.

Useful classes in buffer.jar

DataType: for converting bytestrings to java types.
WrappedObject: wrapper for bytestrings (includes the string, size, type, numel)
Header: incomplete. Wrapper around the buffer header, includes serialization and such.
DataDescription: Class describing sample block properties.
BufferEvent: Class for buffer event, includes serialization.

Data is handled as short[][] array

As reference
BufferClient: contains use of socket stuff.

Plan:

1. Implement NetworkProtol. 
2. Implement Basic BufferServer. (A single listener thread for all connections)
3. Implement Basic DataStore. (Just a list and stuff)

4. Implement JavaServer

5. Implement circular buffer in DataStore.
6. Implement DataStore write to disk.

** Optional **

7. Put DataStore in it's own thread.

8. Expand BufferServer so it provides a single thread per connection.

* Use as little bytestring to type conversion as possible.