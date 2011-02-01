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
 * 
 * 
 * @author Nicky Sandhu
 * @version $Id: PartNamePredicate.java,v 1.1 2003/10/02 20:49:28 redwood Exp $
 */
public class PartNamePredicate implements SortMechanism<DataReference> {
	/**
   *
   */
	public PartNamePredicate(int partId, int sortOrder) {
		_partId = partId;
		_sortOrder = sortOrder;
	}

	/**
	 * true if sort order is ascending
	 */
	public boolean isAscendingOrder() {
		return (_sortOrder == SortMechanism.INCREASING);
	}

	/**
	 * sets the order to ascending or descending.
	 */
	public void setAscendingOrder(boolean ascending) {
		_sortOrder = ascending ? SortMechanism.INCREASING
				: SortMechanism.DECREASING;
	}

	/**
   *
   */
	public int getPartId() {
		return _partId;
	}

	/**
	 * method for collections Comapartor interface
	 */
	public int compare(DataReference first, DataReference second) {
		// check instanceof first and second
		Pathname fPath = ((DataReference) first).getPathname();
		Pathname sPath = ((DataReference) second).getPathname();
		int lexicalValue = 0;
		if (_sortOrder == SortMechanism.INCREASING)
			lexicalValue = sPath.getPart(_partId).compareTo(
					fPath.getPart(_partId));
		else if (_sortOrder == SortMechanism.DECREASING)
			lexicalValue = fPath.getPart(_partId).compareTo(
					sPath.getPart(_partId));
		else
			lexicalValue = 0;
		return lexicalValue;
	}

	/**
   *
   */
	private int _partId;
	/**
   *
   */
	private int _sortOrder;
}
