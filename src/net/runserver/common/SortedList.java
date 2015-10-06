package net.runserver.common;

import java.util.LinkedList;
import java.util.ListIterator;

public class SortedList<E extends Comparable<E>> extends LinkedList<E>
{
	private static final long serialVersionUID = -5004376379557551029L;

	public void put(E value)
	{
		if (size() == 0)
		{
			addFirst(value);
			return;
		}

		if (value.compareTo(getLast()) >= 0)
		{
			addLast(value);
			return;
		}

		ListIterator<E> it = listIterator();
		while (it.hasNext())
		{
			if (value.compareTo(it.next()) < 0)
			{
				it.previous();
				it.add(value);

				return;
			}
		}
	}

	public E getMedian()
	{
		int size = size();
		if (size == 0)
			return null;

		return get(size / 2);
	}
}
