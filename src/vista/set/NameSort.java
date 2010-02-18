/*
    Copyright (C) 1996, 1997, 1998 State of California, Department of 
    Water Resources.

    VISTA : A VISualization Tool and Analyzer. 
	Version 1.0beta
	by Nicky Sandhu
    California Dept. of Water Resources
    Division of Planning, Delta Modeling Section
    1416 Ninth Street
    Sacramento, CA 95814
    (916)-653-7552
    nsandhu@water.ca.gov

    Send bug reports to nsandhu@water.ca.gov

    This program is licensed to you under the terms of the GNU General
    Public License, version 2, as published by the Free Software
    Foundation.

    You should have received a copy of the GNU General Public License
    along with this program; if not, contact Dr. Francis Chung, below,
    or the Free Software Foundation, 675 Mass Ave, Cambridge, MA
    02139, USA.

    THIS SOFTWARE AND DOCUMENTATION ARE PROVIDED BY THE CALIFORNIA
    DEPARTMENT OF WATER RESOURCES AND CONTRIBUTORS "AS IS" AND ANY
    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
    PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE CALIFORNIA
    DEPARTMENT OF WATER RESOURCES OR ITS CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
    OR SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR PROFITS; OR
    BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
    USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
    DAMAGE.

    For more information about VISTA, contact:

    Dr. Francis Chung
    California Dept. of Water Resources
    Division of Planning, Delta Modeling Section
    1416 Ninth Street
    Sacramento, CA  95814
    916-653-5601
    chung@water.ca.gov

    or see our home page: http://wwwdelmod.water.ca.gov/

    Send bug reports to nsandhu@water.ca.gov or call (916)-653-7552

 */
package vista.set;

import COM.objectspace.jgl.Array;
import COM.objectspace.jgl.BinaryPredicate;
import COM.objectspace.jgl.GreaterString;
import COM.objectspace.jgl.LessString;

/**
 * Sorts an array of named by their names The default sorting order is
 * increasing.
 * 
 * @author Nicky Sandhu
 * @version $Id: NameSort.java,v 1.1 2003/10/02 20:49:27 redwood Exp $
 */
public class NameSort implements Sorter {
	/**
	 * increasing order of sort
	 */
	public final static int INCREASING = 1;
	/**
	 * decreasing order of sort
	 */
	public final static int DECREASING = 2;

	/**
	 * sorts using the partId specified and increasing order of sort.
	 */
	public NameSort() {
		this(INCREASING);
	}

	/**
	 * Initializes the sorting method for a certain part id.
	 * 
	 * @param partId
	 *            Id of the part by which to sort as defined by Pathname.?_PART
	 *            constants
	 * @see Pathname
	 */
	public NameSort(int sortOrder) {
		if (sortOrder == INCREASING)
			_sortMechanism = new NameLessThan();
		else if (sortOrder == DECREASING)
			_sortMechanism = new NameGreaterThan();
	}

	/**
	 * sorts the array of data references and returns it as a sorted array No
	 * copy is made of the sorted array.
	 */
	public Array sort(Array refs) {
		// Sorting.sort( refs , _sortMechanism );
		Object[] refArray = new Object[refs.size()];
		refs.copyTo(refArray);
		com.sun.java.util.collections.Arrays.sort(refArray, _sortMechanism);
		refs.swap(new Array(refArray));
		return refs;
	}

	/**
	 * set the sort mechanism;
	 */
	public void setSortMechanism(SortMechanism sortMechanism) {
		if (sortMechanism == null)
			throw new IllegalArgumentException("null sort mechanism");
		_sortMechanism = sortMechanism;
	}

	/**
	 * checks if order of sort is ascending or descending...
	 */
	public boolean isAscendingOrder() {
		return _sortMechanism instanceof NameLessThan;
	}

	/**
	 * sets ascending / descending order
	 */
	public void setAscendingOrder(boolean ascending) {
		if (ascending)
			_sortMechanism = new NameLessThan();
		else
			_sortMechanism = new NameGreaterThan();
	}

	/**
	 * The sorting mechanism used for sorting.
	 */
	private SortMechanism _sortMechanism;

	/**
	 * defines the predicate function to discern the lesser of two data
	 * references
	 */
	private final class NameLessThan implements SortMechanism {
		/**
   *
   */
		public NameLessThan() {
		}

		/**
		 * true if sort order is ascending
		 */
		public boolean isAscendingOrder() {
			return false;
		}

		/**
		 * sets the order to ascending or descending.
		 */
		public void setAscendingOrder(boolean ascending) {
		}

		/**
		 * the sorter activated if the execute method of this one shows that
		 * first == second.
		 */
		public SortMechanism getSecondarySorter() {
			return null;
		}

		/**
		 * sets the secondary sort mechanism
		 */
		public void setSecondarySorter(SortMechanism sm) {
		}

		/**
		 * Return true if both objects are Named objects and the name of first
		 * object is less than the name of second object. The name is compared
		 * using the LessString method
		 */
		public boolean execute(Object first, Object second) {
			// check instanceof first and second
			return (first instanceof Named)
					&& (second instanceof Named)
					&& _lessString.execute(((Named) first).getName(),
							((Named) second).getName());
		}

		/**
		 * method for collections Comapartor interface
		 */
		public int compare(Object first, Object second) {
			// check instanceof first and second
			if (!(first instanceof Named))
				return -1;
			else if (!(second instanceof Named))
				return -1;
			else {
				Named name1 = (Named) first;
				Named name2 = (Named) second;
				return name1.getName().compareTo(name2.getName());
			}
		}

		/**
   *
   */
		public boolean equals(Object o) {
			return false;
		}

		/**
		 * name on which the predicate is to function
		 */
		BinaryPredicate _lessString = new LessString();
	}

	/**
	 * defines the predicate function to discern the greaterer of two data
	 * references
	 */
	private final class NameGreaterThan implements SortMechanism {
		/**
   *
   */
		public NameGreaterThan() {
		}

		/**
		 * Return true if both objects are Named objects and the name of first
		 * object is greater than the name of second object. The name is
		 * compared using the GreaterString method
		 */
		public boolean execute(Object first, Object second) {
			// check instanceof first and second
			return (first instanceof Named)
					&& (second instanceof Named)
					&& _greaterString.execute(((Named) first).getName(),
							((Named) second).getName());
		}

		/**
		 * method for collections Comapartor interface
		 */
		public int compare(Object first, Object second) {
			// check instanceof first and second
			if (!(first instanceof Named))
				return -1;
			else if (!(second instanceof Named))
				return -1;
			else {
				Named name1 = (Named) first;
				Named name2 = (Named) second;
				return name2.getName().compareTo(name1.getName());
			}
		}

		/**
   *
   */
		public boolean equals(Object o) {
			return false;
		}

		/**
		 * true if sort order is ascending
		 */
		public boolean isAscendingOrder() {
			return false;
		}

		/**
		 * sets the order to ascending or descending.
		 */
		public void setAscendingOrder(boolean ascending) {
		}

		/**
		 * the sorter activated if the execute method of this one shows that
		 * first == second.
		 */
		public SortMechanism getSecondarySorter() {
			return null;
		}

		/**
		 * sets the secondary sort mechanism
		 */
		public void setSecondarySorter(SortMechanism sm) {
		}

		/**
		 * name on which the predicate is to function
		 */
		BinaryPredicate _greaterString = new GreaterString();
	}
}
