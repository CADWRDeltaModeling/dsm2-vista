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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import vista.app.schematic.DSMGridElement;
import vista.app.schematic.FluxElement;
import vista.app.schematic.GridLabelElement;
import vista.app.schematic.Network;
import vista.app.schematic.ParticleElement;
import vista.app.schematic.TimeDisplayElement;
import vista.graph.AnimationObservable;
import vista.graph.AnimationObserver;
import vista.graph.Animator;
import vista.graph.AnimatorCanvas;
import vista.graph.GEAttr;
import vista.graph.GEBorderLayout;
import vista.graph.GEContainer;
import vista.graph.GELineLayout;
import vista.graph.GraphicElement;
import vista.graph.TextLine;
import vista.graph.TextLineAttr;
import vista.gui.VistaUtils;
/**
 *
 *
 * @author Nicky Sandhu
 * @version $Id: PTMAnimator.java,v 1.7 1999/01/07 21:05:33 nsandhu Exp $
 */
public class PTMAnimator implements AnimationObserver {
protected DSMGridElement _grid;
protected GEContainer _legend;
public Animator timer;
public ParticleElement pelement;
public AnimatorCanvas _canvas;
public boolean goForward = true;
public GEContainer _mc;
  /**
   *
   */
public PTMAnimator(){
  int delay = 350;
  timer = new Animator();
  timer.setInterval(delay);
  timer.addAnimateDisplay(this);
  _canvas = setUpPTMAnimator();
  // set up buttons
  JButton fasterBtn = new JButton("faster");
  JButton slowerBtn = new JButton("slower");
  JButton stopBtn = new JButton("pause toggle");
  JButton forwardBtn = new JButton(">");
  JButton restartBtn = new JButton("restart");
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
      }
  });
  stopBtn.addActionListener( new ActionListener(){
      public void actionPerformed(ActionEvent evt){
	timer.stopAnimation();
      }
  });
  restartBtn.addActionListener( new ActionListener(){
      public void actionPerformed(ActionEvent evt){
	goForward = true;
	timer.stopAnimation();
	
	timer.startAnimation();
      }
  });
  // setup button panel
  JPanel buttonPanel = new JPanel();
  buttonPanel.setLayout(new FlowLayout());
  buttonPanel.add(forwardBtn); buttonPanel.add(stopBtn); buttonPanel.add(restartBtn);
  buttonPanel.add(fasterBtn);
  buttonPanel.add(slowerBtn);
  //
  JFrame fr = new JFrame(); 
  fr.getContentPane().setLayout( new BorderLayout());
  fr.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
  fr.getContentPane().add(_canvas, BorderLayout.CENTER);
  fr.pack();
  fr.setVisible(true);
  //  timer.startAnimation();
}
  /**
   *
   */
public void update(AnimationObservable observable, Object args){
  _mc.update(observable,args);
  _canvas.redoNextPaint(); _canvas.repaint();
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
  AnimatorCanvas setUpPTMAnimator(){
    return setUpPTMAnimator(null);
  }
  /**
   * create an instance of ptm animator canvas
   */
public AnimatorCanvas setUpPTMAnimator(String propsFile){
  // initialize default properties
  Properties props = new Properties();
  props.put("network.file","net.data");
  props.put("animation.file","anim.bin");
  props.put("flux.file","fluxInfo.data");
  props.put("label.file","labels.data");
  props.put("particle.color","yellow");
  if ( propsFile == null ) propsFile = "ptm_anim.props";
  // get properties
  InputStream is = VistaUtils.getPropertyFileAsStream(propsFile);
  if ( is != null ){
    try {
      props.load(is);
    } catch(IOException ioe){
      System.err.println("Error initializing properties");
    }
  }
  //
  String networkFile = props.getProperty("network.file");
  String animationFile = props.getProperty("animation.file"); 
  String fluxInfoFile =  props.getProperty("flux.file"); 
  String labelInfoFile =  props.getProperty("label.file"); 
  Color particleColor = Color.yellow;
  Font titleFont = new Font("Times Roman", Font.PLAIN, 12);
  Color titleColor = Color.black;
  //
  initMainContainer();
  setTitle( "Particle Tracking Animation" );
  try {
    setNetwork(Network.createNetwork(new FileInputStream(networkFile)));
  }catch(IOException ioe){
    throw new RuntimeException(ioe.getMessage());
  }
  GEAttr gridAttr = new GEAttr();
  gridAttr._backgroundColor = Color.black;//new Color(70,70,70);
  getGrid().setAttributes(gridAttr);
  getGrid().setChannelColor( new Color(20,150,255) );

  pelement = new ParticleElement( getGrid(), animationFile);
  pelement.setColor(particleColor);
  addParticleElement(pelement);

  TextLineAttr tla = new TextLineAttr();
  tla._font = titleFont;
  tla._foregroundColor = Color.yellow;
  TimeData timeData = pelement.getTimeData();
  TimeDisplayElement timeElement = new TimeDisplayElement( tla, timeData);

  addFluxElements( fluxInfoFile , this);
  addLabelElements( labelInfoFile, this);
  addTimeElement(timeElement, 552);
  AnimatorCanvas canvas = new AnimatorCanvas(_mc, timer);
  return canvas;
}
  /**
   * 
   */
  void addFluxElements(String fluxInfoFile, PTMAnimator pa){
    try {
      AsciiFileInput input = new AsciiFileInput();
      input.initializeInput(fluxInfoFile);
      int numberOfFluxes = input.readInt();
      for( int fluxIndex =0; fluxIndex < numberOfFluxes; fluxIndex++ ){
	String filename = input.readString();
	String fluxName = input.readString();
	String labelName = input.readString();
	int nodeId = input.readInt();
	//	System.out.println(filename + fluxName + " " + nodeId);
	FluxElement element = new FluxElement( new GEAttr(), 
					       filename, fluxName ,
					       labelName );
	((GraphicElement)element.getElement(0)).setForegroundColor(Color.green);
	addFluxElement(element, nodeId);
	int min = input.readInt();
	int max = input.readInt();
	//	System.out.println("Min: " + min + " Max: " + max);
	element.setRange( min, max );
      }
    }catch(IOException ioe){
      System.out.println("Error creating flux elements " + ioe);
    }
  }
  /**
   * 
   */
  void addLabelElements(String labelInfoFile, PTMAnimator pa){
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
	System.out.println(labelName + " " + nodeId);
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
  _mc.setInsets(new Insets(10,20,10,20));
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
}
