/*
    Copyright (C) 1996, 1997, 1998 State of California, Department of
    Water Resources.

    VISTA : A VISualization Tool and Analyzer.
	Version 1.0beta
	by Tawnly Pranger
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
package vista.app.commands;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

import vista.gui.Command;
import vista.gui.ExecutionException;
import vista.report.MonthlyReport;
import vista.report.RAppUtils;
import vista.report.TextDisplay;
import vista.set.DataReference;
import vista.set.Group;
import vista.set.RegularTimeSeries;

/**
 * Encapsulates commands implementing group related commands
 * 
 * @author Tawnly Pranger
 */
public class MonthlyAverageCommand implements Command {
	private Group _group;
	private int[] _rNumbers;
	private String _filename;
	private JFrame parent = null;
	private Image image = null;
	private JTextPane tp = null;

	/**
	 * opens group and sets current group to
	 */
	public MonthlyAverageCommand(Group g, int[] referenceNumbers) {
		_group = g;
		_rNumbers = referenceNumbers;
	}

	/**
	 * executes command
	 */
	public void execute() throws ExecutionException {
		if (_rNumbers == null || _rNumbers.length == 0)
			throw new ExecutionException("No data selected");
		for (int i = 0; i < _rNumbers.length; i++) {
			DataReference ref = _group.getDataReference(_rNumbers[i]);
			if (ref.getData().getAttributes().getYUnits().equalsIgnoreCase(
					"TAF"))
				RAppUtils.useCFS = false;
			else
				RAppUtils.useCFS = true;
			MonthlyReport mr = new MonthlyReport((RegularTimeSeries) ref
					.getData(), ref.getPathname(), ref.getFilename());
			StyledDocument sd = mr.getStyledDocument();
			TextDisplay td = new TextDisplay(sd);

			// /*
			tp = new JTextPane();
			tp.setStyledDocument(sd);
			// */
			JFrame frame = new JFrame();
			parent = frame;
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent evt) {
					((JFrame) evt.getSource()).dispose();
				}
			});
			// /*
			JMenuBar bar = new JMenuBar();
			JMenu printMenu = new JMenu("Print");
			JMenuItem printItem = new JMenuItem("Print");
			printItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					/*
					 * // printer option for 1.2 and greater PrinterJob ptjb =
					 * PrinterJob.getPrinterJob(); PageFormat pf =
					 * ptjb.defaultPage(); pf = ptjb.pageDialog(pf);
					 */
					PrintJob pjob;
					Properties p = new Properties();
					Graphics pgraphics;
					pjob = Toolkit.getDefaultToolkit().getPrintJob(parent,
							"Print Monthly Average", p);
					JFrame f2 = new JFrame();
					if (pjob != null) {
						pgraphics = pjob.getGraphics();
						if (pgraphics != null) {
							/*
							 * image = parent.createImage(0,0);
							 * pgraphics.drawImage(image,0,0,parent); JFrame f =
							 * new JFrame(){ public void paint(Graphics gphs){
							 * gphs.drawImage(image,0,0,new Frame()); } };
							 * f.setSize(700,700); f.show();
							 */
							parent.getComponent(0).paint(pgraphics);
							// parent.paint(pgraphics);
						}
						pgraphics.dispose();
					}
					pjob.end();
				}
				/*
				 * 
				 * Style s = null; StyledDocument _doc =
				 * (StyledDocument)tp.getDocument(); try { s =
				 * _doc.getStyle("main"); StyleConstants.setFontSize(s,7); Style
				 * dateStyle = _doc.getStyle("date style"); if ( dateStyle !=
				 * null ) StyleConstants.setFontSize(dateStyle,5);
				 * 
				 * Frame fr = JOptionPane.getFrameForComponent(tp); Toolkit dtk
				 * = Toolkit.getDefaultToolkit(); PrintJob pjob =
				 * dtk.getPrintJob(fr,"Print Dialog",null); if ( pjob != null ){
				 * Graphics pg = pjob.getGraphics(); if (pg!=null) {
				 * tp.paint(pg); pg.dispose(); // flush page } pjob.end(); }
				 * else { try { throw new
				 * RuntimeException("No print job available!!");
				 * }catch(Exception e){ VistaUtils.displayException(null,e); } }
				 * }catch(Exception e){ VistaUtils.displayException(tp,e); }
				 * finally{ if ( s != null){ s = _doc.getStyle("main");
				 * StyleConstants.setFontSize(s,12); Style dateStyle =
				 * _doc.getStyle("date style"); if ( dateStyle != null )
				 * StyleConstants.setFontSize(dateStyle,5); } } }
				 */
			});
			printMenu.add(printItem);
			bar.add(printMenu);
			frame.setJMenuBar(bar);
			JScrollPane jsp = new JScrollPane(tp);
			frame.getContentPane().add(jsp);
			// */
			// frame.setJMenuBar(td.getJMenuBar());
			// frame.getContentPane().add(td);
			frame.setSize(840, 840);
			frame.show();
		}
	}

	/**
	 * unexecutes command or throws exception if not unexecutable
	 */
	public void unexecute() throws ExecutionException {
		throw new ExecutionException("Cannot undo tabulation of data");
	}

	/**
	 * checks if command is executable.
	 */
	public boolean isUnexecutable() {
		return false;
	}

	/**
	 * writes to script
	 */
	public void toScript(StringBuffer buf) {
	}
} // end of TabulateDataCommand
