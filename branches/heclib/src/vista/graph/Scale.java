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
package vista.graph;

/**
 * This class encapsulates scaling information and methods. This handles
 * conversions between a linear data coordinate scale (DC) and a user coordinate
 * scale (UC). Note: For now DC is in units of double and UC in units of int.
 * This will be changed later.
 * 
 * @author Nicky Sandhu (DWR).
 * @version $Id: Scale.java,v 1.1 2003/10/02 20:49:08 redwood Exp $
 */
public class Scale {
	/**
	 * constructor
	 */
	public Scale(double dmin, double dmax, int amin, int amax) {
		_dmin = dmin;
		_dmax = dmax;
		_amin = amin;
		_amax = amax;
		_scale = (_dmax - _dmin) / (_amax - _amin);
	}

	/**
	 * sets scale with new user coordinates
	 */
	public synchronized void setUCRange(int amin, int amax) {
		_amin = amin;
		_amax = amax;
		_scale = (_dmax - _dmin) / (_amax - _amin);
	}

	/**
	 * sets scale with new data coordinates
	 */
	public synchronized void setDCRange(double dmin, double dmax) {
		_dmin = dmin;
		_dmax = dmax;
		_scale = (_dmax - _dmin) / (_amax - _amin);
	}

	/**
	 * scales to user coordinates
	 */
	public int scaleToUC(double v) {
		return (int) Math.round(_amin + (v - _dmin) / _scale);
	}

	/**
	 * scales to data coordinates
	 */
	public double scaleToDC(int v) {
		return (double) (_dmin + (v - _amin) * _scale);
	}

	/**
	 * translate with respect to current scale
	 */
	public void translate(int t) {
		translate(_scale * t);
	}

	/**
	 * translate with respect to current scale
	 */
	public void translate(double dt) {
		_dmin += dt;
		_dmax += dt;
	}

	/**
	 * Description of instance
	 */
	public String toString() {
		return ("Scale: " + _scale + " Range : amin " + _amin + " amax "
				+ _amax + " dmin " + _dmin + " dmax " + _dmax);
	}

	/**
	 * @return maximum data value
	 */
	public double getDataMaximum() {
		return _dmax;
	}

	/**
	 * @return minimum data value
	 */
	public double getDataMinimum() {
		return _dmin;
	}

	/**
   *
   */
	public double getScaling() {
		return _amin + (1.0 - _dmin) / _scale - getShift();
	}

	/**
   *
   */
	public double getShift() {
		return _amin + (0 - _dmin) / _scale;
	}

	/**
	 * The data minimum value;
	 */
	private double _dmin;
	/**
	 * The data minimum value;
	 */
	private double _dmax;
	/**
	 * The user minimum value;
	 */
	private int _amin;
	/**
	 * The user minimum value;
	 */
	private int _amax;
	/**
	 * The scale or ratio of data range to user range.
	 */
	private double _scale;
}
