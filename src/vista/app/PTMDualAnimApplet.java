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
package vista.app;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.JComboBox;
/**
 *
 *
 * @author Nicky Sandhu
 * @version $Id: PTMDualAnimApplet.java,v 1.2 2000/08/03 17:07:30 miller Exp $
 */
public class PTMDualAnimApplet extends JApplet {
  /**
    * The study name to be displayed is in the _studies array with 
    * the corresponding animation file in the _animFiles array.
    */
  String [] _animFiles;
  String [] _studies;
  JComboBox _cbox;
  String strDelim, strPath;
  /**
    *
   */
  public void init(){
    //  public PTMDualAnimApplet(){
    String str;
    str = getParameter("delimitate");
    strDelim = (str != null) ? new String(str) : ";" ;

    str = getParameter("path");
    String strPath = (str != null) ? new String(str) : null ;

    str = getParameter("studies");
    String strStudies = (str != null) ? new String(str) : null ;
    _studies = extractArray(strStudies);
    
    str = getParameter("propsfiles");
    String strProps = (str != null) ? new String(str) : null ;
    _animFiles = extractArray(strPath, strProps);

    _cbox = new JComboBox(_studies);
    Button mainButton = new Button("Press here to start");
    mainButton.addActionListener( new DisplayGUI() );
    Container mainPane = getContentPane();
    mainPane.setLayout( new BorderLayout() );
    mainPane.add(_cbox,BorderLayout.NORTH);
    mainPane.add(mainButton,BorderLayout.SOUTH);
    setVisible(true);
  }

  private String [] extractArray(String str){
    return extractArray("",str);
  }

  private String [] extractArray(String preStr, String str){
    int cnt = 0;
    int indx1 = 0, indx2 = 0;
    //count number of sections
    do {
      indx2 = str.indexOf(strDelim,indx1);
      indx1 = indx2 + 1;
      cnt = cnt + 1;
    } while (indx2 != -1);

    indx1 = 0;
    indx2 = 0;
    //size array
    //    System.out.println("cnt "+cnt);
    String [] strArray = new String [cnt];
    for (int i = 0; i < strArray.length; i++){
      indx2 = str.indexOf(strDelim,indx1);
      if (indx2 > 0) {
	strArray [i] = preStr + str.substring(indx1,indx2);
      }
      else {
	strArray [i] = preStr + str.substring(indx1);
      }
      //      System.out.println(strArray[i]);
      indx1 = indx2 + 1;
    }

    return strArray;
  }
  /**
   *
   *
   * @author Nicky Sandhu
   * @version $Id: PTMDualAnimApplet.java,v 1.2 2000/08/03 17:07:30 miller Exp $
   */
  class DisplayGUI implements ActionListener{
    /**
      *
      */
    public void actionPerformed(ActionEvent evt){
      new PTMDualAnimator(_animFiles[_cbox.getSelectedIndex()]);
    }
  }// end of DisplayGUI class
}
