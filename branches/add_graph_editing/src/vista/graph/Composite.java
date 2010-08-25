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
package vista.graph;

/**
 * A composite of bounded objects. Each object contained in this composite has
 * bounds. The composite itself is a bounded object and may be contained in
 * another bounded composite. This is the "Composite" Pattern.
 * 
 * @author Nicky Sandhu (DWR).
 * @version $Id: Composite.java,v 1.1 2003/10/02 20:48:51 redwood Exp $
 */
public interface Composite {
	/**
	 * adds a graphic element to the composite
	 */
	public void add(GraphicElement leaf);

	/**
	 * adds a graphic element to the composite with information
	 */
	public void add(String name, GraphicElement leaf);

	/**
	 * inserts an element at the desired index
	 */
	public void insertAt(int index, GraphicElement leaf);

	/**
	 * removes the particular object from the container
	 */
	public int remove(GraphicElement leaf);

	/**
	 * removes all elements from the composite.
	 */
	public void removeAll();

	/**
	 * searches for the first element that matches and returns its index
	 */
	public int indexOf(GraphicElement leaf);

	/**
	 * gets the child element count
	 * 
	 * @return the number of child graphic elements
	 */
	public int getElementCount();

	/**
	 * gets the element at the specified index. This index corresponds to the
	 * order in which the elements were added to this composite.
	 * 
	 * @returns the element.
	 */
	public GraphicElement getElement(int n);

	/**
	 * returns a copy of the array of graphic elements contained in this
	 * composite.
	 */
	public GraphicElement[] getElements();

	/**
	 * gets a iterator object to iterate over the leaves or composites contained
	 * in this composite
	 */
	public CompositeIterator getIterator();

	/**
	 * sets the iterator for iterating over the leaves of this composite.
	 */
	public void setIterator(CompositeIterator iterator);
}
