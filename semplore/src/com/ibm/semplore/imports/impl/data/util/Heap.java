package com.ibm.semplore.imports.impl.data.util;

public   class   Heap   
{   
	private   Comparable[]   heap;   
	private   int   size;   
	private   int   capacity;   
	private   int   capacityIncrement;   

	/**   
	 *   Construct   a   heap   with   initial   capacity   10     
	 *   and   capacity   increment   0   
	 *   
	 *   @since       1.0   
	 *   */   
	public   Heap()   
	{   
		this(10,   0);   
	}   

	/**   
	 *   Construct   a   heap   with   initial   capacity   c   
	 *   and   capacity   increment   0   
	 *     
	 *   @param   c       initial   capacity   for   heap   
	 *   @exception   IllegalArgumentException       c   is   negative   
	 *   @since       1.0   
	 *   */   
	public   Heap(int   c)   
	{   
		this(c,   0);   
	}   

	/**   
	 *   Construct   a   heap   with   initial   capacity   c   
	 *   and   capacity   increment   ci   
	 *     
	 *   @param   c       initial   capacity   for   heap   
	 *   @param   ci     capacity   increment   for   heap   
	 *   @exception   IllegalArgumentException       c   or   ci   is   negative   
	 *   @since       1.0   
	 *   */   
	public   Heap(int   c,   int   ci)   
	{   
		if   (c   <   0   ||   ci   <   0)   
			throw   new   IllegalArgumentException();   

		size                             =   0;   
		capacity                     =   c;   
		capacityIncrement   =   ci;   

		heap   =   new   Comparable[capacity   +   1];   
	}   

	/**   
	 *   Return   the   size   of   the   heap   
	 *   
	 *   @return       the   number   of   elements   contained   in   the   heap   
	 *   @since       1.0   
	 *   */   
	public   int   size()   
	{   
		return   size;   
	}   

	/**   
	 *   Return   the   capacity   of   the   heap   
	 *   
	 *   @return       the   size   of   the   internal   array   used   to   store   the   heap   
	 *   @since       1.0   
	 *   */   
	public   int   capacity()   
	{   
		return   capacity;   
	}   

	/**   
	 *   Insert   an   element   into   the   heap   
	 *     
	 *   @param   value       object   to   insert   
	 *   @exception   IllegalArgumentException       value   is   null   
	 *   @since       1.0   
	 *   */   
	public   void   insert(Comparable   value)   
	{   
		if   (value   ==   null)   
			throw   new   IllegalArgumentException();   

		if   (size   ==   capacity)   
		{   
			if   (capacityIncrement   ==   0)   
				capacity   *=   2;   
			else   
				capacity   +=   capacityIncrement;   

			Comparable[]   temp   =   new   Comparable[capacity   +   1];   
			System.arraycopy(heap,   1,   temp,   1,   size);   
			heap   =   temp;   
		}   

		heap[++size]   =   value;           //   add   at   end   of   array   
		siftUp(heap,   size,   size);   //   restore   heap   property   
	}   

	/**   
	 *   Remove   top   element   from   the   heap   
	 *     
	 *   @return       the   element   with   the   greatest   value   from   the   heap   
	 *   @exception   EmptyHeapException       heap   is   empty   
	 *   @since       1.0   
	 *   */   
	public   Comparable   remove()   throws   Exception   
	{   
		switch   (size)   
		{   
		case   0:   
			throw   new   Exception();   

		case   1:   
			return   heap[size--];   //   special   case   for   size   =   1   

		default:   
			Comparable   ret   =   heap[1];   //   will   return   top   element   
		heap[1]   =   heap[size--];       //   move   last   element   at   top   and   adjust   size   
		siftDown(heap,   size,   1);     //   restore   heap   property   
		return   ret;   
		}   
	}   

	/*   
	 *   Sift   an   element   up   to   its   correct   place   in   the   heap   
	 *     
	 *   @param   A       array   containing   the   heap   
	 *   @param   size       size   of   heap   
	 *   @param   pos       position   of   element   that   we   sift   up   
	 *   @exception   IllegalArgumentException       pos   <   1   or   pos   >   size     
	 *                                                                               or   size   >   A.length   
	 *   @since       1.0   
	 *   */   
	private   static   void   siftUp(Comparable[]   A,   int   size,   int   pos)   
	{   
		if   (pos   <   1   ||   pos   >   size   ||   size   >   A.length)   
			throw   new   IllegalArgumentException();   

		int   child     =   pos;   
		int   parent   =   child   /   2;   
		Comparable   value   =   A[child];   

		while   (parent   >   0)   
		{   
			if   (A[parent].compareTo(value)   <   0)   
			{   
				A[child]   =   A[parent];   
				child   =   parent;   
				parent   =   parent   /   2;   
			}   
			else   
				break;   
		}   

		A[child]   =   value;   
	}   

	/*   
	 *   Sift   an   element   down   to   its   correct   place   in   the   heap   
	 *     
	 *   @param   A       array   containing   the   heap   
	 *   @param   size       size   of   heap   
	 *   @param   pos       position   of   element   that   we   sift   down   
	 *   @exception   IllegalArgumentException       pos   <   1   or   pos   >   size     
	 *                                                                               or   size   >   A.length   
	 *   @since       1.0   
	 *   */   
	private   static   void   siftDown(Comparable[]   A,   int   size,   int   pos)   
	{   
		if   (pos   <   1   ||   pos   >   size   ||   size   >   A.length)   
			throw   new   IllegalArgumentException();   

		int   parent   =   pos;   
		int   child     =   2   *   parent;   
		Comparable   value   =   A[parent];   

		while   (child   <=   size)   
		{   
			if   (child   <   size   &&   A[child].compareTo(A[child   +   1])   <   0)   
				child++;   

			if   (A[child].compareTo(value)   >   0)   
			{   
				A[parent]   =   A[child];   
				parent         =   child;   
				child           =   2   *   child;   
			}   
			else   
				break;   
		}   

		A[parent]   =   value;   
	}   

	/*   
	 *   Transform   an   array   into   a   heap   
	 *     
	 *   @param   A       array   that   we   want   to   transform   into   a   heap   
	 *   @param   size       number   of   elements   
	 *   @exception   IllegalArgumentException       size   >   A.length   
	 *   @since       1.0   
	 *   */   
	private   static   void   heapify(Comparable[]   A,   int   size)   
	{   
		if   (size   >   A.length)   
			throw   new   IllegalArgumentException();   

		for   (int   i   =   size   /   2;   i   >   0;   i--)   
			siftDown(A,   size,   i);   
	}   
}   
