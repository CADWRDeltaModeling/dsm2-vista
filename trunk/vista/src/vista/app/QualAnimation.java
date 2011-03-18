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
package vista.app;

import java.awt.Color;
import java.awt.Font;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Vector;

import vista.app.schematic.ChannelData;
import vista.app.schematic.ChannelElement;
import vista.app.schematic.DSMGridElement;
import vista.app.schematic.Network;
import vista.app.schematic.TimeDisplayElement;
import vista.graph.Animator;
import vista.graph.AnimatorCanvas;
import vista.graph.AnimatorFrame;
import vista.graph.GEAttr;
import vista.graph.GEBorderLayout;
import vista.graph.GEContainer;
import vista.graph.GELineLayout;
import vista.graph.TextLine;
import vista.graph.TextLineAttr;

/**
 * Animator for coloring channels from data
 */
public class QualAnimation {
	/**
	 * main method for starting animation
	 */
	public static void main(String[] args) {
		QualAnimation test = new QualAnimation();
		AnimatorCanvas canvas = test.createChannelAnimationCanvas();
		AnimatorFrame frame = new AnimatorFrame(canvas, "Qual Animation Frame");
		canvas.getAnimator().startAnimation();
	}

	private String _networkFile = "/home/palm/nsandhu/java/vista/app/net.data";
	private String _channelDataMapFile = "/home/palm/nsandhu/java/vista/app/qual.list";
	private String _startTime = "01JAN1991 0100";
	private String _timeInterval = "1DAY";

	/**
   * 
   */
	public AnimatorCanvas createChannelAnimationCanvas() {
		// create network grid
		Network network = null;
		try {
			network = Network.createNetwork(new java.io.FileInputStream(
					_networkFile));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage()
					+ ": Could not initialize network from " + _networkFile);
		}
		DSMGridElement grid1 = new DSMGridElement(network);
		grid1.setChannelColor(new Color(153, 196, 220));
		network.getLink(1);
		// create channel elements for the grid
		ChannelElement[] elements = createChannelElements(_channelDataMapFile,
				grid1);
		// create time element and anchor it to a node # 500.
		TextLineAttr tla = new TextLineAttr();
		tla._font = new Font("Times Roman", Font.PLAIN, 20);
		tla._foregroundColor = Color.blue;
		tla._backgroundColor = Color.white;
		TimeData timeData = new DefaultTimeData(_startTime, _timeInterval);
		TimeDisplayElement timeElement = new TimeDisplayElement(tla, timeData);
		timeElement.setGrid(grid1);
		timeElement.setBaseNode(500);
		//
		// ColorBar colorBar = new ColorBar( new GEAttr() );
		// colorBar.setGrid(grid1);
		// colorBar.setBaseNode(510);

		// add grid and time element
		GEContainer gridContainer1 = new GEContainer(new GEAttr());
		gridContainer1.setLayout(new GEBorderLayout());
		gridContainer1.add("Center", grid1);
		gridContainer1.add("Center", timeElement);
		// add all the channel elements
		for (int i = 0; i < elements.length; i++)
			gridContainer1.add("Center", elements[i]);
		// gridContainer1.add("Center",colorBar);
		// create main container for the grid container and the title
		GEContainer mainContainer = new GEContainer(new GEAttr());
		mainContainer.setLayout(new GELineLayout(GELineLayout.VERTICAL,
				GELineLayout.CENTERED_ON_BOUNDS));
		mainContainer.add(new TextLine(new TextLineAttr(),
				"Testing Channel Animation"));
		mainContainer.add(gridContainer1);
		// create animator and add the animate elements
		Animator animator = new Animator();
		for (int i = 0; i < elements.length; i++)
			animator.addAnimateElement(elements[i]);
		animator.addAnimateElement(timeElement);
		// create display and add to animator
		AnimatorCanvas canvas = new AnimatorCanvas(mainContainer, animator);
		animator.addAnimateDisplay(canvas);
		// return the animator canvas
		return canvas;
	}

	/**
	 * create channel elements from file and grid
	 */
	public ChannelElement[] createChannelElements(String filename,
			DSMGridElement grid) {
		try {
			LineNumberReader input = new LineNumberReader(new FileReader(
					filename));
			String line;
			// read the dss filename
			line = input.readLine().trim();
			while (line.startsWith("#"))
				line = input.readLine().trim();
			String dssfilename = line;
			//
			line = input.readLine().trim();
			while (line.startsWith("#"))
				line = input.readLine().trim();
			String startTime = line;
			//
			line = input.readLine().trim();
			while (line.startsWith("#"))
				line = input.readLine().trim();
			String timeInterval = line;
			//
			Vector ve = new Vector();
			int i = 0;
			while ((line = input.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#"))
					continue;
				int ci = line.indexOf(",");
				if (ci <= 0)
					continue;
				int channelNumber = new Integer(line.substring(0,
						line.indexOf(",")).trim()).intValue();
				String pathname = line.substring(line.indexOf(",") + 1,
						line.length()).trim();
				System.out.println("Channel #: " + channelNumber
						+ " & pathname = " + pathname);
				ChannelData data = new ChannelData(channelNumber, dssfilename,
						pathname, startTime, timeInterval);
				ve.addElement(new ChannelElement(new GEAttr(), grid, data));
			}
			ChannelElement[] elements = new ChannelElement[ve.size()];
			ve.copyInto(elements);
			return elements;
		} catch (IOException ioe) {
			System.out.println(ioe);
			System.exit(-1);
		}
		return null;
	}
}
