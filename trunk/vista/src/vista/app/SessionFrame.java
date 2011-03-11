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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.table.TableModel;

import vista.db.dss.DSSUtil;
import vista.db.hdf5.HDF5Group;
import vista.gui.Command;
import vista.gui.SystemConsole;
import vista.gui.VistaUtils;

/**
 * The main GUI for the application
 * 
 * @author Nicky Sandhu
 * @version $Id: SessionFrame.java,v 1.1 2003/10/02 20:48:40 redwood Exp $
 */
public class SessionFrame extends JFrame implements DropTargetListener {
	/**
	 * The view representing the current session
	 */
	private SessionView _sessionView;
	private DropTarget dropTarget;
	/**
	 * for debuggin'
	 */
	private static final boolean DEBUG = true;
	/**
   *
   */
	private static JProgressBar _progressBar;
	private static JLabel _statusBar;
	private static SystemConsole systemConsole;

	/**
	 * !PENDING: till progress monitors are installed.
	 */
	public static JProgressBar getProgressBar() {
		return _progressBar;
	}

	/**
   *
   */
	static {
		_progressBar = new JProgressBar();
		_progressBar.setMinimum(0);
		_progressBar.setMaximum(100);
		_statusBar = new JLabel("");
	}

	/**
	 * Constructor
	 */
	public SessionFrame(String[] dssfiles) {
		systemConsole = new SystemConsole();
		// System.out.println("dssfiles: ");
		if (dssfiles != null) {
			for (int i = 0; i < dssfiles.length; i++) {
				dssfiles[i] = dssfiles[i].replace('\\', '/');
				dssfiles[i] = dssfiles[i].trim();
				/*
				 * dssfiles[i] = dssfiles[i].substring(0, dssfiles[i]
				 * .indexOf(".dss") + 4);
				 */
				// System.out.println("dssfile["+i+"]="+dssfiles[i]);
			}
		}
		if (DEBUG)
			systemConsole.resetSystemOutput();
		// set dss access properties
		try {
			DSSUtil.setAccessProperties(MainProperties.getProperties());
			// decide on look and feel
			setLookAndFeel();
			setIconImage(Toolkit.getDefaultToolkit().createImage(
					VistaUtils.getImageAsBytes("/vista/DWR_Logo-1.0in.gif")));
			setTitle("DATA VISTA (" + VistaUtils.getVersionId() + ")");
		} catch (Exception e) {
			System.err.println("Could not load up dss library");
		}

		// create table as view
		_sessionView = new SessionTable();
		dropTarget = new DropTarget(_sessionView, this);

		// set up session menu
		JMenu sessionMenu = createSessionMenu(this);
		// set up group menu
		JMenu groupMenu = _sessionView.getMenu();
		// set up options menu
		JMenu optionMenu = createOptionsMenu(this);
		// set up about menu
		JMenu helpMenu = createHelpMenu(this);
		// set up menus
		JMenuBar mbar = new JMenuBar();
		mbar.add(sessionMenu);
		mbar.add(groupMenu);
		mbar.add(optionMenu);
		mbar.add(helpMenu);
		getRootPane().setJMenuBar(mbar);

		// add listener for user unconditional quit request
		WindowListener l = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				quitSession(null);
			}
		};
		this.addWindowListener(l);

		// add components, session view and progress bar
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		statusPanel.add(_progressBar, BorderLayout.EAST);
		statusPanel.add(_statusBar, BorderLayout.CENTER);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(_sessionView, BorderLayout.CENTER);
		contentPane.add(statusPanel, BorderLayout.SOUTH);
		// pack();

		// set size to default
		int width = new Integer(MainProperties.getProperty("gui.width"))
				.intValue();
		int height = new Integer(MainProperties.getProperty("gui.height"))
				.intValue();
		setSize(width, height);
		setPosition(width, height);
		if (MainProperties.getProperty("gui.connect").equals("true")
				&& dssfiles == null) {
			String server = MainProperties.getProperty("default.server");
			String directory = MainProperties.getProperty("default.dir");
			Executor.execute(new OpenConnectionSessionCommand(MainGUI
					.getContext(), server, directory, true), _sessionView);
			if (DEBUG)
				System.out.println("connecting to " + server + directory);
		} else if (dssfiles != null) {
			// open session with local and directory
			String server = "local";
			String directory = null;
			int dindex = dssfiles[0].lastIndexOf("/");
			if (dindex >= 0)
				directory = dssfiles[0].substring(0, dindex);
			if (directory == null)
				directory = "./";
			if (DEBUG)
				System.out.println("Opening local directory " + directory);
			Executor.execute(new OpenConnectionSessionCommand(MainGUI
					.getContext(), server, directory, true), _sessionView);
			for (int j = 0; j < dssfiles.length; j++) {
				if (dssfiles[j] == null)
					continue;
				Executor.execute(new OpenDSSFileCommand(MainGUI.getContext(), dssfiles[j], _sessionView), _sessionView);
			}
			dssfiles=null;
		}
		setVisible(true);
		// if dss files then send it to the back
		if (dssfiles != null) {
			toBack();
		}
		VistaUtils.disposeStartUpIcon();
	}

	/**
   *
   */
	public SessionView getView() {
		return _sessionView;
	}

	/**
	 * shows copyright dialog
	 */
	public void showAbout(ActionEvent evt) {
		JFrame frame = null;
		if (evt.getSource() instanceof JFrame)
			frame = (JFrame) evt.getSource();
		else
			frame = new JFrame();
		final JDialog dialog = new JDialog(frame, "CopyRight Notice");
		dialog.getContentPane().setLayout(new BorderLayout());
		JTextArea text = new JTextArea(VistaUtils.getCopyrightNotice(), 30, 90);
		text.setEditable(false);
		JScrollPane sp = new JScrollPane(text);
		JButton okButton = new JButton("OK");
		class OkListener implements ActionListener {
			public void actionPerformed(ActionEvent evt) {
				dialog.dispose();
			}
		}
		;
		okButton.addActionListener(new OkListener());
		dialog.getContentPane().add(sp, BorderLayout.CENTER);
		dialog.getContentPane().add(okButton, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setSize(525, 750);
		dialog.show();
	}

	/**
	 * creates session menu and associated listeners
	 */
	private JMenu createSessionMenu(Object obj) {
		JMenu sessionMenu = new JMenu("Session");
		JMenuItem newSession = new JMenuItem("New");
		newSession.setMnemonic('n');
		JMenu openMenu = new JMenu("Open");
		JMenuItem openSession = new JMenuItem("Session");
		openSession.setMnemonic('o');
		JMenuItem openConnection = new JMenuItem("Connection");
		openConnection.setMnemonic('O');
		JMenuItem openDSSFile = new JMenuItem("DSS File");
		openDSSFile.setMnemonic('f');
		JMenuItem openHydroTideFile = new JMenuItem("Hydro TideFile");
		openHydroTideFile.setMnemonic('t');
		openMenu.add(openConnection);
		openMenu.add(openDSSFile);
		openMenu.add(openHydroTideFile);
		openMenu.add(openSession);
		JMenuItem loadSession = new JMenuItem("Load");
		loadSession.setMnemonic('l');
		JMenuItem saveSession = new JMenuItem("Save");
		saveSession.setMnemonic('s');
		JMenuItem saveAsSession = new JMenuItem("Save As");
		saveSession.setMnemonic('a');
		JMenuItem closeSession = new JMenuItem("Close");
		JMenuItem quitSession = new JMenuItem("Exit");
		quitSession.setMnemonic('x');
		// add listeners
		newSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				createNewSession(evt);
			}
		});
		openSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				openSession(evt);
			}
		});
		openConnection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				openConnection(evt);
			}
		});
		openDSSFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				openDSSFile(evt);
			}
		});
		openHydroTideFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				openHydroTideFile(evt);
			}
		});
		loadSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				loadSession(evt);
			}
		});
		saveSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				saveSession(evt);
			}
		});
		saveAsSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				saveAsSession(evt);
			}
		});
		closeSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				closeSession(evt);
			}
		});
		quitSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				quitSession(evt);
			}
		});
		// add to menu
		sessionMenu.add(openMenu);
		sessionMenu.addSeparator();
		sessionMenu.add(newSession);
		sessionMenu.add(loadSession);
		sessionMenu.add(saveSession);
		sessionMenu.add(saveAsSession);
		sessionMenu.addSeparator();
		sessionMenu.add(closeSession);
		sessionMenu.add(quitSession);
		return sessionMenu;
	}

	/**
   *
   */
	private JMenu createOptionsMenu(Object obj) {
		// set up options menu
		JMenu optionMenu = new JMenu("Options");
		JMenuItem showDialogMenu = new JMenuItem("Edit options...");
		JMenuItem saveOptionsMenu = new JMenuItem("Save options...");
		JMenuItem undoMenu = new JMenuItem("Undo last...");
		JMenuItem consoleMenu = new JMenuItem("Show Console");
		showDialogMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				showOptionsDialog(evt);
			}
		});
		saveOptionsMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				saveOptions();
			}
		});
		undoMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				undoCommand(evt);
			}
		});
		consoleMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				consoleCommand(evt);
			}
		});
		//
		optionMenu.add(showDialogMenu);
		optionMenu.add(saveOptionsMenu);
		optionMenu.addSeparator();
		optionMenu.add(undoMenu);
		optionMenu.addSeparator();
		optionMenu.add(consoleMenu);

		return optionMenu;
	}

	/**
   *
   */
	private JMenu createHelpMenu(Object obj) {
		JMenu helpMenu = new JMenu("Help");
		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				showAbout(evt);
			}
		});
		helpMenu.add(aboutItem);
		return helpMenu;
	}

	/**
   *
   */
	private void setPosition(int w, int h) {
		String ps = MainProperties.getProperty("gui.initialPosition");
		String tbstr = ps.substring(0, ps.indexOf("&")).trim();
		String lrstr = ps.substring(ps.indexOf("&") + 1, ps.length()).trim();
		int yPos = 0;
		int xPos = 0;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (tbstr.equals("bottom")) {
			yPos = screenSize.height - h;
		} else {
			yPos = 0;
		}
		if (lrstr.equals("right")) {
			xPos = screenSize.width - w;
		} else {
			xPos = 0;
		}
		setLocation(xPos, yPos);
	}

	/**
	 * sets look and feel to motif
	 */
	public void undoCommand(ActionEvent evt) {
		Executor.unexecute();
	}

	/**
    *
    */
	public void consoleCommand(ActionEvent evt) {
		systemConsole.setVisible(true);
	}

	/**
	 * sets look and feel to motif
	 */
	public void setMotifLF(ActionEvent evt) {
		VistaUtils.setLookAndFeel(this, "motif.MotifLookAndFeel");
	}

	/**
	 * sets look and feel to metal
	 */
	public void setSystemLF(ActionEvent evt) {
		VistaUtils.setLookAndFeel(this, "system");
	}

	/**
	 * sets look and feel to metal
	 */
	public void setXPlatformLF(ActionEvent evt) {
		VistaUtils.setLookAndFeel(this, "xplaf");
	}

	/**
	 * sets look and feel to metal
	 */
	public void setWindowsLF(ActionEvent evt) {
		VistaUtils.setLookAndFeel(this, "windows.WindowsLookAndFeel");
	}

	/**
	 * sets look and feel for this gui.
	 */
	private void setLookAndFeel() {
		/*
		 * try { for (LookAndFeelInfo info :
		 * UIManager.getInstalledLookAndFeels()) { if
		 * ("Nimbus".equals(info.getName())) {
		 * UIManager.setLookAndFeel(info.getClassName()); return; } } } catch
		 * (UnsupportedLookAndFeelException e) { // handle exception } catch
		 * (ClassNotFoundException e) { // handle exception } catch
		 * (InstantiationException e) { // handle exception } catch
		 * (IllegalAccessException e) { // handle exception }
		 */
		String laf = MainProperties.getProperty("gui.lookAndFeel");

		if (laf.indexOf("vista") >= 0) {
			VistaUtils.setLookAndFeel(this, "metal.MetalLookAndFeel");
		} else if (laf.indexOf("motif") >= 0) {
			VistaUtils.setLookAndFeel(this, "motif");
		} else if (laf.indexOf("windows") >= 0) {
			VistaUtils.setLookAndFeel(this, "windows");
		} else if (laf.indexOf("system") >= 0) {
			VistaUtils.setLookAndFeel(this, "system");
		} else if (laf.indexOf("xplaf") >= 0) {
			VistaUtils.setLookAndFeel(this, "xplaf");
		} else if (laf.indexOf("mac") >= 0) {
			VistaUtils.setLookAndFeel(this, "mac");
		} else {
			VistaUtils.setLookAndFeel(this, laf);
		}
	}

	/**
	 * creates new session
	 */
	private void createNewSession(ActionEvent evt) {
		SessionContext context = MainGUI.getContext();
		Command com = new SetNewSessionCommand(context);
		Executor.execute(com, _sessionView);
		_statusBar.setText("created new session");
	}

	/**
	 * open session from connection as new session
	 */
	private void openSession(ActionEvent evt) {
		// get filename from dialog...
		String loadFilename = VistaUtils.getFilenameFromDialog(this,
				FileDialog.LOAD, "ssn", "Session Files");
		boolean addOn = false;
		Executor.execute(new OpenSessionCommand(MainGUI.getContext(),
				loadFilename, addOn), _sessionView);
		_statusBar.setText("Opened session: " + loadFilename);
	}

	/**
	 * load session into current session. This is the default. as connections to
	 * multiple servers or directories are opened the groups get added on to the
	 * current session.
	 */
	private void loadSession(ActionEvent evt) {
		// get filename from dialog...
		String loadFilename = VistaUtils.getFilenameFromDialog(this,
				FileDialog.LOAD, "ssn", "Session Files");
		boolean addOn = true;
		new Thread(new ExecutionRunner(new OpenSessionCommand(MainGUI
				.getContext(), loadFilename, addOn), _sessionView)).start();
	}

	/**
	 * save session here...
	 */
	private void saveSession(ActionEvent evt) {
		SessionContext sc = MainGUI.getContext();
		Command com = new SaveSessionCommand(sc.getCurrentSession(), false);
		new Thread(new ExecutionRunner(com, _sessionView)).start();
		_statusBar
				.setText("Saved session: " + sc.getCurrentSession().getName());
	}

	/**
	 * save session here...
	 */
	private void saveAsSession(ActionEvent evt) {
		SessionContext sc = MainGUI.getContext();
		Command com = new SaveSessionCommand(sc.getCurrentSession(), true);
		new Thread(new ExecutionRunner(com, _sessionView)).start();
		_statusBar
				.setText("Saved session: " + sc.getCurrentSession().getName());
	}

	/**
	 * close this session
	 */
	private void closeSession(ActionEvent evt) {
		Command com = new SetNewSessionCommand(MainGUI.getContext());
		Executor.execute(com, _sessionView);
		_statusBar.setText("Closed session: "
				+ MainGUI.getContext().getCurrentSession().getName());
	}

	/**
	 * quit the session
	 */
	private void quitSession(ActionEvent evt) {
		// !PENDING: save before quit
		// if ( option == JOptionPane.YES_OPTION )
		// !PENDING: ApplicationExitCommand
		_statusBar.setText("Exiting application !");
		System.exit(0);
	}

	/**
	 * opens connection to server
	 */
	private void openConnection(ActionEvent evt) {
		new OpenConnectionDialog(this, _sessionView).show();
		_statusBar.setText("Opened connection");
	}

	/**
	 * opens a local file
	 */
	private void openDSSFile(ActionEvent evt) {
		String filename = VistaUtils.getFilenameFromDialog(null,
				java.awt.FileDialog.LOAD, "dss", "DSS File");
		if (filename == null)
			return;
		Command com = new OpenDSSFileCommand(MainGUI.getContext(), filename,
				_sessionView);
		Executor.execute(com, _sessionView);
		_statusBar.setText("Opened local file: " + filename);
	}

	/**
	 * opens a local file
	 */
	private void openHydroTideFile(ActionEvent evt) {
		String filename = VistaUtils.getFilenameFromDialog(null,
				java.awt.FileDialog.LOAD, "h5", "Hydro Tidefile");
		if (filename == null)
			return;
		Command com = new OpenHydroTideFileCommand(MainGUI.getContext(),
				filename, _sessionView);
		Executor.execute(com, _sessionView);
		_statusBar.setText("Opened local file: " + filename);
	}

	/**
	 * sets properties
	 */
	public void setProperties(Properties p) {
		MainProperties.setProperties(p);
		// set dss access properties
		DSSUtil.setAccessProperties(p);
		//
		setLookAndFeel();
	}

	private void showOptionsDialog(ActionEvent evt) {
		new OptionsDialog(this, MainProperties.getProperties()).show();
	}

	private void saveOptions() {
		MainProperties.saveTo(System.getProperty("user.dir")
				+ System.getProperty("file.separator") + ".vista");
		_statusBar.setText("Saved options");
	}

	// -------------- DnD stuff
	public void dragEnter(DropTargetDragEvent dtde) {
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void dragOver(DropTargetDragEvent dtde) {
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	public void drop(DropTargetDropEvent dtde) {
		try {
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavors = tr.getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				if (flavors[i].isFlavorJavaFileListType()) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					List list = (List) tr.getTransferData(flavors[i]);
					for (int j = 0; j < list.size(); j++) {
						Object item = list.get(j);
						if (item instanceof File) {
							File file = (File) item;
							String absolutePath = file.getAbsolutePath();
							if (absolutePath.toLowerCase().contains(".dss")) {
								((SessionTable) _sessionView).getSession()
										.addGroup(
												DSSUtil.createGroup("local",
														absolutePath));
							} else if (absolutePath.toLowerCase().contains(
									".h5")) {
								((SessionTable) _sessionView).getSession()
										.addGroup(new HDF5Group(absolutePath));
							}
							_sessionView.updateView();
						}
					}
					dtde.dropComplete(true);
					return;
				} else if (flavors[i].isFlavorSerializedObjectType()) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					Object o = tr.getTransferData(flavors[i]);
					System.out.println("add Object: " + o);
					dtde.dropComplete(true);
					return;
				} else if (flavors[i].isRepresentationClassInputStream()) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

					System.out.println(new InputStreamReader((InputStream) tr
							.getTransferData(flavors[i]))
							+ "from system clipboard");
					dtde.dropComplete(true);
					return;
				}
			}
			dtde.rejectDrop();
		} catch (Exception e) {
			e.printStackTrace();
			dtde.rejectDrop();
		}
	}
}
