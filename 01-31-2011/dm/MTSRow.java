/*
    Copyright (C) 1996-2000 State of California, Department of 
    Water Resources.

    VISTA : A VISualization Tool and Analyzer. 
	Version 1.0
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
package vista.dm;
import com.sun.xml.tree.XmlDocument;
import com.sun.xml.tree.TreeWalker;
import org.w3c.dom.Element;
import vista.set.Pathname;
/**
  * A row defining a time series within an MTS. Each row can either be
  * a DTS name or a pathname mapping. If the row is a pathname mapping then
  * if any part is null it is intrepreted to mean that it is to be ignored. 
  * The D part is interpreted as a time window.
  * @author Nicky Sandhu
  * @version $Id: MTSRow.java,v 1.4 2000/03/21 18:16:22 nsandhu Exp $
  */
public class MTSRow{
  private String _dtsName;
  private String [] _pathParts;
  /**
    * an empty row
    */
  public MTSRow(){
    _dtsName = null;
    _pathParts = new String[Pathname.MAX_PARTS];
  }
  /**
   *
   */
  public MTSRow(MTSRow row){
    _dtsName = row._dtsName;
    _pathParts = new String[Pathname.MAX_PARTS];
    System.arraycopy(row._pathParts,0,_pathParts,0,Pathname.MAX_PARTS);
    for(int i=0; i < _pathParts.length; i++) {
      if ( _pathParts[i] == null ) _pathParts[i] = "";
      _pathParts[i] = _pathParts[i].trim();
    }
  }
  /**
    * sets the row to map to a time series defined by a DTS.
    * If this is set to null or "" it implies that the definition
    * can then be a path part mapping.
    */
  public void setDTSName(String nm){
    _dtsName = nm == "" ? null : nm;
  }
  /**
    * @return the name of the DTS or "" if N/A
    */
  public String getDTSName(){
    return _dtsName == null ? "": _dtsName;
  }
  /**
    * checks the range of given id
    */
  private void checkId(int id){
    if ( id < 0 || id >= Pathname.MAX_PARTS ){
      throw new RuntimeException("invalid path part id: 0 < " + id + " < "+ Pathname.MAX_PARTS);
    }
  }
  /**
    * sets the path part if any. This setting does not determine if this part
    * is actually used in retrieving the time series data. That is determined at
    * the study level logic and could very well ignore this. The display UI also
    * reflects that.
    */
  public void setPathPart(String part, int id){
    checkId(id);
    if ( part == null ) 
      _pathParts[id] = "";
    else
      _pathParts[id] = part.trim();
  }
  /**
    * the path part for given id
    */
  public String getPathPart(int id){
    checkId(id);
    return _pathParts[id];
  }
  /**
    * get all the path parts
    */
  public String [] getPathParts(){
    return _pathParts;
  }
  /**
    * a string representation of this object
    */
  public String toString(){
    String str = "MTS Row: ";
    if ( ! getDTSName().equals("") ){
      return str + "DTS -> " + getDTSName();
    } else {
      for(int i=0; i < Pathname.MAX_PARTS; i++){
	if ( getPathPart(i).equals("") ) continue;
	str += " " + Pathname.getPartName(i) + "->" + getPathPart(i);
      }
    }
    return str;
  }
  /**
    * initializes from an xml tag tree.
    */
  public void fromXml(Element re){
    this.setDTSName(re.getAttribute("dts"));
    for(int i=0; i < Pathname.MAX_PARTS; i++)
      this.setPathPart(re.getAttribute(Pathname.getPartName(i).substring(0,1)),i);
  }
  /**
    * builds an xml tag tree
    */
  public void toXml(XmlDocument doc, Element ae){
    Element de = doc.createElement("row");
    de.setAttribute("dts",getDTSName());
    for(int i=0; i < Pathname.MAX_PARTS; i++)
      de.setAttribute(Pathname.getPartName(i).substring(0,1),getPathPart(i));
    ae.appendChild(de);
  }
  /**
   *
   */
  public boolean equals(Object obj){
    if ( obj instanceof MTSRow ){
      MTSRow row = (MTSRow) obj;
      if ( row.getDTSName().length() > 0 ){
	return row.getDTSName().equals(getDTSName());
      } else {
	boolean isSameAs = true;
	for(int i=0; i < _pathParts.length; i++){
	  isSameAs = isSameAs && (row.getPathPart(i).equals(getPathPart(i)));
	}
	return isSameAs;
      }
    } else {
      return false;
    }
  }
}
