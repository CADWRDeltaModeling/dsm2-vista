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
import vista.set.*;
/**
  * This class contains the default values for the pathname parts. Each level of container
  * can have defaults. The project has a set of defaults, each individual study may have
  * its set of defaults and each MTS/DTS could have its set of defaults as well.
  * If a path part is empty then its an indicator that it may be ignored in finding 
  * matching references.
  *
  * @author Nicky Sandhu
  * @version $Id: MTSDefaults.java,v 1.2 2000/03/21 18:16:22 nsandhu Exp $
  */
public class MTSDefaults{
  private String [] _pathParts;
  /**
    *
    */
  public MTSDefaults(String [] parts){
    _pathParts = new String[Pathname.MAX_PARTS];
    for(int i=0; i < _pathParts.length; i++) _pathParts[i]="";
    for(int i=0; i < Math.min(parts.length,_pathParts.length); i++) {
      if( parts[i] != null ) _pathParts[i] = parts[i];
    }
  }
  /**
    *
    */
  public String getPathPart(int i){
    return _pathParts[i];
  }
  /**
    *
    */
  public static MTSRow createDefaultRow(){
    MTSRow row = new MTSRow();
    row.setDTSName("");
    for(int i=0; i < Pathname.MAX_PARTS; i++) row.setPathPart("",i);
    return row;
  }
}
