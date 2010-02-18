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

/**
 * An interface of an iterator which iterates through the elements of a DataSet.
 * Once an iterator hits either end it ignores calls for advancing or retreating
 * past its ends and remains positioned at the extremal elements
 * 
 * @see DataSet
 * @author Nicky Sandhu (DWR).
 * @version $Id: DataSetIterator.java,v 1.1 2003/10/02 20:49:21 redwood Exp $
 */
public interface DataSetIterator extends java.io.Serializable {
	/**
	 * Resets the iterator to the beginning of data. This means that the
	 * atStart() method returns true. It also means that getElement() method
	 * returns the first element and that the nextElement() method also returns
	 * the first element. However subsequent calls to the nextElement() method
	 * return the next elements.
	 */
	public void resetIterator();

	/**
	 * gets the element at the current location
	 */
	public DataSetElement getElement();

	/**
	 * puts the element at the current location
	 */
	public void putElement(DataSetElement e);

	/**
	 * positions iterator at index
	 */
	public void positionAtIndex(int i);

	/**
	 * Advance by one.
	 */
	public void advance();

	/**
	 * Retreat by one
	 */
	public void retreat();

	/**
	 * 0 if no elements were skipped by getting this element from the underlying
	 * data set<br>
	 * 
	 * + n if the iterator has just skipped n elements of the underlying data
	 * set<br>
	 * 
	 * - n if the iterator has just skipped n elements in the reverse direction
	 * of the underlying data set<br>
	 * 
	 */
	public int hasSkipped();

	/**
	 * Gets the current index for the iterator. This keeps track of the number
	 * of advances or retreates that the iterator has made on the underlying
	 * data set. Varies from 0 to size()-1
	 */
	public int getIndex();

	/**
	 * This is useful for iterators running on top of other iterators. For
	 * normal iterators its just getIndex()
	 */
	public int getUnderlyingIndex();

	/**
	 * if iterator is at start of data
	 */
	public boolean atStart();

	/**
	 * if iterator is at end of data.
	 */
	public boolean atEnd();

	/**
	 * The maximum of x and y range encapsulated as a data set element.
	 */
	public DataSetElement getMaximum();

	/**
	 * The minimum of x and y range encapsulated as a data set element.
	 */
	public DataSetElement getMinimum();
}
