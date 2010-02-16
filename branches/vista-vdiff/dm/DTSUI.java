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
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import vista.gui.XYGridLayout;
/**
 *
 *
 * @author Nicky Sandhu
 * @version $Id: DTSUI.java,v 1.2 2000/03/21 18:16:20 nsandhu Exp $
 */
public class DTSUI extends JPanel{
  private DerivedTimeSeries _dts;
  private JTextField _exprField;
  private JTextField [] _partsField;
  protected Hashtable _actionTable = new Hashtable();
  /**
   *
   */
  public DTSUI(DerivedTimeSeries dts){
    _dts = dts;
    _exprField = new JTextField(dts.getExpression(),20);
    _exprField.getDocument().addDocumentListener( new DocumentListener(){
      public void insertUpdate(DocumentEvent evt){
	_dts.setExpression(_exprField.getText());
      }
      public void removeUpdate(DocumentEvent evt){
	_dts.setExpression(_exprField.getText());
      }
      public void changedUpdate(DocumentEvent evt){
      }
    });
    // a pathname editor
    _partsField = new JTextField[4];
    //
    Pathname path = dts.getPathname();
    _partsField[0] = new JTextField(path.getPart(Pathname.A_PART),10);
    _partsField[0].setBorder( BorderFactory.createTitledBorder("A"));
    _partsField[0].getDocument().addDocumentListener( new DocumentListener(){
      public void insertUpdate(DocumentEvent evt){
	_dts.getPathname().setPart(Pathname.A_PART, _partsField[0].getText());
      }
      public void removeUpdate(DocumentEvent evt){
	_dts.getPathname().setPart(Pathname.A_PART, _partsField[0].getText());
      }
      public void changedUpdate(DocumentEvent evt){
      }
    });
    _partsField[1] = new JTextField(path.getPart(Pathname.B_PART),10);
    _partsField[1].setBorder( BorderFactory.createTitledBorder("B"));
    _partsField[1].getDocument().addDocumentListener( new DocumentListener(){
      public void insertUpdate(DocumentEvent evt){
	_dts.getPathname().setPart(Pathname.B_PART, _partsField[0].getText());
      }
      public void removeUpdate(DocumentEvent evt){
	_dts.getPathname().setPart(Pathname.B_PART, _partsField[0].getText());
      }
      public void changedUpdate(DocumentEvent evt){
      }
    });
    _partsField[2] = new JTextField(path.getPart(Pathname.C_PART),10);
    _partsField[2].setBorder( BorderFactory.createTitledBorder("C"));
    _partsField[2].getDocument().addDocumentListener( new DocumentListener(){
      public void insertUpdate(DocumentEvent evt){
	_dts.getPathname().setPart(Pathname.C_PART, _partsField[0].getText());
      }
      public void removeUpdate(DocumentEvent evt){
	_dts.getPathname().setPart(Pathname.C_PART, _partsField[0].getText());
      }
      public void changedUpdate(DocumentEvent evt){
      }
    });
    _partsField[3] = new JTextField(path.getPart(Pathname.F_PART),10);
    _partsField[3].setBorder( BorderFactory.createTitledBorder("F"));
    _partsField[3].getDocument().addDocumentListener( new DocumentListener(){
      public void insertUpdate(DocumentEvent evt){
	_dts.getPathname().setPart(Pathname.F_PART, _partsField[0].getText());
      }
      public void removeUpdate(DocumentEvent evt){
	_dts.getPathname().setPart(Pathname.F_PART, _partsField[0].getText());
      }
      public void changedUpdate(DocumentEvent evt){
      }
    });
    //
    JPanel partPanel = new JPanel();
    partPanel.setBorder(BorderFactory.createTitledBorder("Pathname parts"));
    partPanel.setLayout(new GridLayout(1,4));
    partPanel.add(_partsField[0]);
    partPanel.add(_partsField[1]);
    partPanel.add(_partsField[2]);
    partPanel.add(_partsField[3]);
    //
    JPanel np = new JPanel();
    np.setLayout(new BorderLayout());
    //
    np.add( _exprField, BorderLayout.NORTH);
    np.add( partPanel, BorderLayout.SOUTH);
    add(np,BorderLayout.NORTH);
    //
    MTSUI mui = new MTSUI(dts.getMTS());
    add(mui,BorderLayout.CENTER);
  }
  /**
   * returns the action associated with the given name.
   */
  public Action getAction(String actionName){
    return (Action) _actionTable.get(actionName);
  }
  /**
   *
   */
  protected void initActions(){
    _actionTable.put("Retrieve",getRetrieveAction());
  }
  /**
   *
   */
  public Action getRetrieveAction(){
    Action action = new AbstractAction("Retrieve"){
      public void actionPerformed(ActionEvent evt){
	System.out.println("To be implemented");
	//	_dts.getStudy().retrieve(_dts);
      }
    };
    return action;
  }
  
}
