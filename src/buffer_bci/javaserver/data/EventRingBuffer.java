package buffer_bci.javaserver.data;

public class EventRingBuffer {
	private final Event[] ring;
	private final int capacity;
	private int size = 0;
	private int newPos = 0;

	/**
	 * Constructor
	 * 
	 * @param size
	 *            size of the ring
	 */
	public EventRingBuffer(int size) {
		ring = new Event[size];
		capacity = size;
	}

	/**
	 * Adds an item to the buffer.
	 * 
	 * @param item
	 */
	public void add(Event item) {
		size++;
		ring[newPos++] = item;

		// If newPos has reached capacity wrap the ring around.
		if (newPos == capacity) {
			newPos = 0;
		}
	}

	/**
	 * Resets the buffer.
	 */
	public void clear() {
		size = 0;
		newPos = 0;
	}

	/**
	 * Used to get an item from the ring.
	 * 
	 * @param index
	 *            Index ranges from 0 to the number of items added in the ring
	 *            -1.
	 * @return the value at index
	 */
	public Event get(int index) throws IndexOutOfBoundsException {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index < 0.");
		}

		if (index < size - capacity) {
			throw new IndexOutOfBoundsException(
					"Index < index of oldest item in buffer.");
		}

		if (index >= size) {
			throw new IndexOutOfBoundsException("Index >= size.");
		}

		if (size < capacity) {
			// Ring hasn't wrapped yet.
			return ring[index];
		} else {
			// Ring has wrapped.

			index -= size - capacity; // Subtract the index of the oldest item
										// still in the ring.

			index += newPos; // Add ring-index of the oldest item still in the
								// ring.

			// Check if index should be wrapped around.
			if (index >= capacity) {
				return ring[index - capacity];
			} else {
				return ring[index];
			}
		}
	}

	/**
	 * Returns the index of the oldest item.
	 * 
	 * @return
	 */
	public int indexOfOldest() {
		if (size <= capacity) {
			return 0;
		} else {
			return size - capacity;
		}
	}

	/**
	 * Returns the total number of items that have been added to the ring.
	 * 
	 * @return
	 */
	public int size() {
		return size;
	}

}
