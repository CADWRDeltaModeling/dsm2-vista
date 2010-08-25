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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import vista.graph.AnimationObservable;
import vista.graph.AnimationObserver;
import vista.graph.Animator;
import vista.graph.AnimatorCanvas;
import vista.graph.GEAttr;
import vista.graph.GEBorderLayout;
import vista.graph.GEContainer;
import vista.graph.GELineLayout;
import vista.graph.GraphicElement;
import vista.graph.Leaf;
import vista.graph.TextLine;
import vista.graph.TextLineAttr;
/**
 *
 *
 * @author Nicky Sandhu
 * @version $Id: PTMDualAnimator.java,v 1.8 2001/03/27 22:22:03 miller Exp $
 */
public class PTMDualAnimator implements AnimationObserver {
protected DSMGridElement _grid;
protected GEContainer _legend;
public Animator timer;
public ParticleElement pelement;
public AnimatorCanvas _canvas1, _canvas2;
public FluxElement [][] _element1, _element2;
public boolean goForward = true;
public boolean fileStatus;
public GEContainer _mc1, _mc2, _mc;
public JFrame _frame;
public JCheckBox _loop;
public JPanel _animPanel;
public JButton fasterBtn, slowerBtn, stopBtn, forwardBtn, restartBtn;
public String propsFile1, propsFile2;
public int delay;
  /**
   * sets up two animation canvases for side by side comparision
   */
public PTMDualAnimator(String propsFile){
  // set up default properties
  Properties props = new Properties();
  props.put("animation.delay","500");
  props.put("props.file1","ptm_anim1.props");
  props.put("props.file2","ptm_anim2.props");
  if ( propsFile == null ){
    propsFile = "ptm-animation.properties";
  }
  // get properties from file
  //?? change for web
    //VistaUtils.getPropertyFileAsStream(propsFile);
  try {
    AsciiFileInput mpfi = new AsciiFileInput();
    InputStream is = mpfi.getInputStream(propsFile);
    if ( is == null ) {
      throw new IOException("Error initializing from : " + propsFile);
    }
    props.load(is);
  } catch(IOException ioe){
    System.err.println("Error initializing properties");
  }
  // initialize from properties
  delay = new Integer(props.getProperty("animation.delay")).intValue();
  propsFile1 = props.getProperty("props.file1");
  propsFile2 = props.getProperty("props.file2");
  //
  setupCanvas();

  // set up buttons
  fasterBtn = new JButton("Faster");
  slowerBtn = new JButton("Slower");
  stopBtn = new JButton("Pause");
  stopBtn.setEnabled(false);
  forwardBtn = new JButton("Start");
  restartBtn = new JButton("Restart");
  _loop = new JCheckBox("Repeat");
  fasterBtn.addActionListener( new ActionListener(){
      public void actionPerformed(ActionEvent evt){
	increaseAnimationSpeed(50);
      }
  });
  slowerBtn.addActionListener( new ActionListener(){
      public void actionPerformed(ActionEvent evt){
	increaseAnimationSpeed(-50);
      }
  });
  forwardBtn.addActionListener( new ActionListener(){
      public void actionPerformed(ActionEvent evt){
	goForward = true;
	timer.startAnimation();
	forwardBtn.setEnabled(false);
	stopBtn.setEnabled(true);
      }
  });
  stopBtn.addActionListener( new ActionListener(){
      public void actionPerformed(ActionEvent evt){
	timer.stopAnimation();
	stopBtn.setEnabled(false);
	forwardBtn.setEnabled(true);
      }
  });
  restartBtn.addActionListener( new ActionListener(){
      public void actionPerformed(ActionEvent evt){
	restartAnimation();
      }
  });
  // setup button panel
  JPanel buttonPanel = new JPanel();
  buttonPanel.setLayout(new FlowLayout());
  buttonPanel.add(forwardBtn); buttonPanel.add(stopBtn);
  buttonPanel.add(restartBtn);
  buttonPanel.add(fasterBtn);
  buttonPanel.add(slowerBtn);
  buttonPanel.add(_loop);
  //
  _frame = new JFrame(); 
  _frame.getContentPane().setLayout( new BorderLayout());
  _frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

  setGraphicElementContainer();
  _frame.addWindowListener(new WindowAdapter() {
    public void windowClosing(WindowEvent e) {
      _loop.setSelected(false);
      //      System.exit(0);
    }
  });

  _frame.pack();
  _frame.setVisible(true);
  //  timer.startAnimation();
}

  public void restartAnimation(){
    timer.stopAnimation();
    GEContainer cont = (GEContainer) _canvas1.getGraphicElement();
    Leaf [] leaves = cont.getElements(ParticleElement.class);
    ParticleElement pe = (ParticleElement) leaves[0];
    pe.rewind();
    cont = (GEContainer) _canvas2.getGraphicElement();
    leaves = cont.getElements(ParticleElement.class);
    pe = (ParticleElement) leaves[0];
    pe.rewind();
    System.out.println(_element1[0].length+" "+_element2[0].length);
    for(int i = 0; i < _element1[0].length; i++) {
      _element1[0][i].resetInput();
    }
    for(int i = 0; i < _element2[0].length; i++) {
      _element2[0][i].resetInput();
    }
    timer.startAnimation();
    forwardBtn.setEnabled(false);
    stopBtn.setEnabled(true);
  }

  public void setupCanvas(){
    timer = new Animator();
    timer.setInterval(delay);
    timer.addAnimateDisplay(this);
    _element1 = new FluxElement [1][];
    _element2 = new FluxElement [1][];
    _canvas1 = setUpPTMAnimator(propsFile1, _element1);
    _canvas2 = setUpPTMAnimator(propsFile2, _element2);
    _canvas1.addMouseListener(new MouseListener(){
	public void mouseClicked(MouseEvent e){
	    System.out.println("x="+e.getX()+" y="+e.getY());
	}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
    });
  }
    
  public void setGraphicElementContainer(){
  _animPanel = new JPanel(); _animPanel.setLayout(new FlowLayout());
  _animPanel.add(_canvas1); _animPanel.add(_canvas2);
  _mc1 = (GEContainer) _canvas1.getGraphicElement();
  _mc2 = (GEContainer) _canvas2.getGraphicElement();
  _frame.getContentPane().add(_animPanel, BorderLayout.CENTER);
  }

  /**
   *
   */
public void update(AnimationObservable observable, Object args){
  if (pelement.fileStatus == true) { EndOfFileEncounter(); }
  else {
  _mc1.update(observable,args); _mc2.update(observable,args);
  _canvas1.paint(_canvas1.getGraphics());
  _canvas2.paint(_canvas2.getGraphics());
  }
}
  /**
   *
   */
  public void EndOfFileEncounter(){
    System.out.println("Thread Stop Called");
    timer.stopAnimation();
    forwardBtn.setEnabled(false);
    stopBtn.setEnabled(false);
    if(_loop.isSelected()) restartAnimation();
  }
  /**
   *
   */
public void increaseAnimationSpeed(int increment){
  int delay = (int) timer.getInterval();
  if ( delay - increment > 0 ){
    timer.setInterval(delay-increment);
  }
}
  /**
   * default property file initialization
   */
  //  AnimatorCanvas setUpPTMAnimator(){
    //return setUpPTMAnimator(null);
  //  }
  /**
   * create an instance of ptm animator canvas
   */
public AnimatorCanvas setUpPTMAnimator(String propsFile, FluxElement [][] element){
  // initialize default properties
  Properties props = new Properties();
  props.put("network.file","net.data");
  props.put("animation.file","anim.bin");
  props.put("flux.file","fluxInfo.data");
  props.put("label.file","labels.data");
  props.put("particle.color","yellow");
  props.put("flux.display","true");
  props.put("zoom.factor","1");
  props.put("origin.xpos","0");
  props.put("origin.ypos","0");
  if ( propsFile == null ) propsFile = "ptm_anim.props";
  // get properties
  try {
    //?? change for web
    AsciiFileInput pfi = new AsciiFileInput();
    InputStream is = pfi.getInputStream(propsFile);
    if ( is != null ){
      props.load(is);
    } else {
      throw new IOException("Error initializing properties");
    }
  } catch(IOException ioe){
    System.err.println("Error initializing properties");
  }

  //
  String networkFile = props.getProperty("network.file");
  String animationFile = props.getProperty("animation.file"); 
  String fluxInfoFile =  props.getProperty("flux.file"); 
  String labelInfoFile =  props.getProperty("label.file"); 
  boolean showFluxElements = new Boolean(props.getProperty("flux.display")).booleanValue();
  float zoomfactor = new Float(props.getProperty("zoom.factor")).floatValue();
  int xpos = new Integer(props.getProperty("origin.xpos")).intValue();
  int ypos = new Integer(props.getProperty("origin.ypos")).intValue();
  System.out.println("zoom = "+zoomfactor+","+xpos+","+ypos);
  //
  Color particleColor = Color.yellow;
  Font titleFont = new Font("Times Roman", Font.PLAIN, 12);
  Color titleColor = Color.black;
  //
  initMainContainer();
  setTitle( "Particle Tracking Animation" );
  try {
    AsciiFileInput nfi = new AsciiFileInput();
    InputStream nis = nfi.getInputStream(networkFile);
    //? change for web
    if ( nis != null ){
      setNetwork(Network.createNetwork(nis));
    }else {
      throw new IOException("Error initializing network file");
    }
  }catch(IOException ioe){
    throw new RuntimeException(ioe.getMessage());
  }
  GEAttr gridAttr = new GEAttr();
  gridAttr._backgroundColor = Color.black;//new Color(70,70,70);
  getGrid().setAttributes(gridAttr);
  getGrid().setChannelColor( new Color(20,150,255) );
  getGrid().setZoom(zoomfactor, xpos, ypos);

  pelement = new ParticleElement( getGrid(), animationFile);
  pelement.setColor(particleColor);
  addParticleElement(pelement);

  TextLineAttr tla = new TextLineAttr();
  tla._font = titleFont;
  tla._foregroundColor = Color.yellow;
  TimeData timeData = pelement.getTimeData();
  TimeDisplayElement timeElement = new TimeDisplayElement( tla, timeData);

  if ( showFluxElements ) addFluxElements( fluxInfoFile , this, element);
  addLabelElements( labelInfoFile, this);
  addTimeElement(timeElement, 9006);
  AnimatorCanvas canvas = new AnimatorCanvas(_mc, timer);
  return canvas;
}
  /**
   * 
   */
  void addFluxElements(String fluxInfoFile, PTMDualAnimator pa, FluxElement [][] element){
    try {
      AsciiFileInput input = new AsciiFileInput();
      input.initializeInput(fluxInfoFile);
      int numberOfFluxes = input.readInt();
      element[0] = new FluxElement [numberOfFluxes];
      for( int fluxIndex =0; fluxIndex < numberOfFluxes; fluxIndex++ ){
	String filename = input.readString();
	String fluxName = input.readString();
	String labelName = input.readString();
	int nodeId = input.readInt();
	//	System.out.println(filename + fluxName + " " + nodeId);
	element [0][fluxIndex] = new FluxElement( new GEAttr(), 
					       filename, fluxName ,
					       labelName );
	((GraphicElement)element[0][fluxIndex].getElement(0)).setForegroundColor(Color.green);
	addFluxElement(element[0][fluxIndex], nodeId);
	int min = input.readInt();
	int max = input.readInt();
	//	System.out.println("Min: " + min + " Max: " + max);
	element[0][fluxIndex].setRange( min, max );
      }
    }catch(IOException ioe){
      System.out.println("Error creating flux elements " + ioe);
    }
  }
  /**
   * 
   */
  void addLabelElements(String labelInfoFile, PTMDualAnimator pa){
    try {
      AsciiFileInput input = new AsciiFileInput();
      input.initializeInput(labelInfoFile);
      int numberOfLabels = input.readInt();
      TextLineAttr tla = new TextLineAttr();
      tla._font = new Font("Times Roman", Font.BOLD, 12);
      tla._foregroundColor = Color.white;
      for( int labelIndex =0; labelIndex < numberOfLabels; labelIndex++ ){
	String labelName = input.readString();
	int nodeId = input.readInt();
	//	int fontSize = input.readInt();
	//tla._font = new Font("Times Roman", Font.BOLD, fontSize);
	GridLabelElement element = 
	  new GridLabelElement( tla, labelName );
	addLabelElement(element, nodeId);
      }
    }catch(IOException ioe){
      System.out.println("Error creating label elements " + ioe);
    }
  }
  /**
   * 
   */
void initMainContainer(){
  _mc = new GEContainer(new GEAttr());
  _mc.setLayout( new GEBorderLayout() );
  _mc.setInsets(new Insets(5,5,5,5));
  addLegend();
}
  /**
   * 
   */
public void setTitle(String text){
  TextLineAttr tla = new TextLineAttr();
  tla._font = new Font("Times Roman", Font.BOLD, 15);
  tla._foregroundColor = Color.blue;
  TextLine title = new TextLine(tla, text);
  _mc.add("North", title);
}
  /**
   * 
   */
public void setNetwork( Network network ){
  _grid = new DSMGridElement(network);
  _mc.add("Center", _grid);
}
  /**
   * 
   */
public DSMGridElement getGrid(){
  return _grid;
}
  /**
   * 
   */
public void addParticleElement( ParticleElement element ){
  _mc.add("Center", element);
  _legend.add( element );
}
  /**
   * 
   */
public void addFluxElement( FluxElement element , int nodeId ){
  element.setGrid( _grid );
  element.setBaseNode( nodeId );
  _mc.add("Center", element);
}
  /**
   * 
   */
public void addTimeElement( TimeDisplayElement element , int nodeId ){
  element.setGrid( _grid );
  element.setBaseNode( nodeId );
  _mc.add("Center", element);
}
  /**
   * 
   */
public void addLabelElement( GridLabelElement element , int nodeId ){
  element.setGrid( _grid );
  element.setBaseNode( nodeId );
  _mc.add("Center", element);
}
  /**
   * 
   */
public void addLegend( ){
  _legend = new GEContainer( new GEAttr() );
  _legend.setLayout ( new GELineLayout ( GELineLayout.VERTICAL, 
					 GELineLayout.CENTERED_ON_BOUNDS 
					 )
		      );
}
  /**
   * 
   */
protected void addToLegend( ParticleElement element ){
  GEContainer legendItem = new GEContainer( new GEAttr() );
  legendItem.setLayout( new GEBorderLayout() );
  //  legendItem.add( "East", new Shape());
  TextLineAttr tla = new TextLineAttr();
  tla._font = new Font("Times Roman", Font.PLAIN, 10);
  legendItem.add( "West", new TextLine( tla, element.getName() ) );
  _legend.add( legendItem );
}
  /**
    *
    */
  public static void main(String [] args){
    if ( args.length == 1 )
      new PTMDualAnimator(args[0]);
  }
}
