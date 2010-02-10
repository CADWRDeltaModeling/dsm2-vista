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
 * An abstract class for encapsulating DataSet for use in plotting. This would
 * enable different data sets to implement this interface and use the plotting
 * capabilities as long as these functions were properly implemented along with
 * DataSetElement and DataSetIterator
 * 
 * @see DataSetElement
 * @see DataSetIterator
 * @author Nicky Sandhu (DWR).
 * @version $Id: DataSet.java,v 1.1 2003/10/02 20:49:21 redwood Exp $
 */
public interface DataSet extends Named, java.io.Serializable {
	/**
	 * returns the number of elements in the dataset
	 */
	public int size();

	/**
	 * gets element at index i
	 */
	public DataSetElement getElementAt(int i);

	/**
	 * sets element at index i
	 */
	public void putElementAt(int i, DataSetElement dse);

	/**
	 * Return an iterator positioned at my first item.
	 */
	public DataSetIterator getIterator();

	/**
	 * sets the name to identify the data set.
	 */
	public void setName(String name);

	/**
	 * returns a name for this DataSet to be used to identify it.
	 */
	public String getName();

	/**
	 * An object attached to this data set which contains descriptive
	 * information of the underlying data.
	 */
	public DataSetAttr getAttributes();

	/**
	 * An object attached to this data set which contains descriptive
	 * information of the underlying data.
	 */
	public void setAttributes(DataSetAttr attr);

	/**
	 * true if data set is flagged
	 */
	public boolean isFlagged();
}
