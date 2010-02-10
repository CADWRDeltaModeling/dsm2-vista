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
package vista.db.dss;

import java.util.Enumeration;

import vista.set.Pathname;
import COM.objectspace.jgl.Array;

/**
 * Reads a catalog (condensed if name ends in .dsd) file and creates a default
 * group based on that. Returns the group of data references via methods.
 * Enumeration support is provided in case of humongous catalog files so that
 * filtering /sorting can be supported without having to read in the catalog all
 * at once.
 */
class DSSCatalogReader implements Enumeration {
	private String[] _catalogListing;
	private int _currentIndex = 0;
	private String _currentLine;

	/**
	 * Initializes the reader with this file
	 */
	public DSSCatalogReader(String[] catalogListing) {
		if (catalogListing == null)
			throw new IllegalArgumentException("Null catalog");
		_catalogListing = catalogListing;
		readCatalog();
	}

	/**
	 * Reads in the catalog
	 */
	private void readCatalog() {
		while (hasMoreLines()) {
			if (nextLine().startsWith("Tag"))
				break;
		}
		String line = _currentLine;
		if (line == null)
			throw new IllegalArgumentException("Catalog is empty ?");
		_beginIndex[Pathname.A_PART] = line.indexOf("A Part");
		_endIndex[Pathname.A_PART] = line.indexOf("B Part");
		_beginIndex[Pathname.B_PART] = line.indexOf("B Part");
		_endIndex[Pathname.B_PART] = line.indexOf("C Part");
		_beginIndex[Pathname.C_PART] = line.indexOf("C Part");
		_endIndex[Pathname.C_PART] = line.indexOf("F Part");
		_beginIndex[Pathname.D_PART] = line.indexOf("D Part");
		_endIndex[Pathname.D_PART] = MAX_LINE_LENGTH; // line.length();
		_beginIndex[Pathname.E_PART] = line.indexOf("E Part");
		_endIndex[Pathname.E_PART] = line.indexOf("D Part");
		_beginIndex[Pathname.F_PART] = line.indexOf("F Part");
		_endIndex[Pathname.F_PART] = line.indexOf("E Part");
		// skip one line
		nextLine();
		_npaths = _catalogListing.length - _currentIndex;
	}

	private int _npaths;

	public int getNumberOfPaths() {
		return _npaths;
	}

	public boolean hasMoreLines() {
		return (_currentIndex < _catalogListing.length);
	}

	public String nextLine() {
		// clean out memory
		if (_currentIndex > 0)
			_catalogListing[_currentIndex - 1] = null;
		_currentLine = _catalogListing[_currentIndex].trim();
		_currentIndex++;
		return _currentLine;
	}

	/**
	 * Checks to see if more data references are available.
	 */
	public boolean hasMoreElements() {
		return (hasMoreLines() && !_catalogListing[_currentIndex].trim()
				.startsWith("*"));
	}

	/**
	 * Returns the next data reference
	 */
	public Object nextElement() {
		nextLine();
		Pathname path = makePathname(_currentLine);
		return path;
	}

	/**
	 * Returns an array of pathnames for this catalog
	 */
	public Pathname[] getPathnames() {
		Array array = new Array();
		while (hasMoreElements()) {
			array.add(nextElement());
		}
		Pathname[] pathnames = new Pathname[array.size()];
		array.copyTo(pathnames);
		return pathnames;
	}

	/**
   *
   */
	private String[] _parts = new String[Pathname.MAX_PARTS];

	/**
   *
   */
	protected Pathname makePathname(String line) {
		if (DEBUG)
			System.out.println(line);
		if (_masterPath == null) {
			_endIndex[Pathname.D_PART] = _currentLine.length();
			for (int i = 0; i < _parts.length; i++) {
				_parts[i] = _currentLine
						.substring(_beginIndex[i], _endIndex[i]);
			}
			for (int i = 0; i < _parts.length; i++) {
				if (_parts[i].indexOf("(null)") >= 0)
					_parts[i] = "";
			}
			if (_parts[Pathname.D_PART].indexOf("*") >= 0) {
				int inx = _parts[Pathname.D_PART].indexOf("*");
				_parts[Pathname.D_PART] = _parts[Pathname.D_PART].substring(0,
						inx);
			}
			_masterPath = Pathname.createPathname(_parts);
		} else {
			_endIndex[Pathname.D_PART] = _currentLine.length();
			if (DEBUG)
				System.out.println(_currentLine);
			for (int i = 0; i < _parts.length; i++) {
				if (DEBUG)
					System.out.println("index " + i);
				if (DEBUG)
					System.out.println("begin index " + _beginIndex[i]);
				if (DEBUG)
					System.out.println("end index " + _endIndex[i]);
				_parts[i] = _currentLine
						.substring(_beginIndex[i], _endIndex[i]);
			}
			updateFrom(_parts, _masterPath);
			for (int i = 0; i < _parts.length; i++) {
				if (_parts[i].indexOf("(null)") >= 0)
					_parts[i] = "";
			}
			if (_parts[Pathname.D_PART].indexOf("*") >= 0) {
				int inx = _parts[Pathname.D_PART].indexOf("*");
				_parts[Pathname.D_PART] = _parts[Pathname.D_PART].substring(0,
						inx);
			}
			Pathname path = Pathname.createPathname(_parts);
			_masterPath = path;
		}
		return _masterPath;

	}

	// /**
	// *
	// */
	// private static Pattern _pattern;
	// static {
	// try {
	// _pattern = new AwkCompiler().compile("(- )+(-)");
	// } catch ( MalformedPatternException mpe ){
	// mpe.printStackTrace();
	// throw new RuntimeException("Incorrect Regular Expression ");
	// }
	// }
	/**
   *
   */
	// private static PatternMatcher _matcher = new AwkMatcher();
	/**
   *
   */
	private void updateFrom(String[] parts, Pathname path) {
		for (int i = 0; i < parts.length; i++) {
			// if ( _matcher.matches( parts[i], _pattern ) ) parts[i] =
			// path.getPart(i);
			if (parts[i].indexOf("- -") >= 0)
				parts[i] = path.getPart(i);
		}
	}

	/**
	 * returns a string with .dss replaced by .dsd
	 */
	public static String getCatalogFilename(String dssfile) {
		return dssfile.substring(0, dssfile.lastIndexOf(".")).trim() + ".dsd";
	}

	/**
	 * returns a string with .dsd replaced by .dss
	 */
	public static String getDSSFilename(String dsdfile) {
		return dsdfile.substring(0, dsdfile.lastIndexOf(".")).trim() + ".dss";
	}

	/**
	 * true if name ends with ".dss"
	 */
	public static boolean isValidDSSFile(String dssfile) {
		return (dssfile.endsWith(".dss"));
	}

	/**
	 * true if name ends with ".dsd"
	 */
	public static boolean isValidDSDFile(String dsdfile) {
		return (dsdfile.endsWith(".dsd"));
	}

	/**
   *
   */
	private int[] _beginIndex = new int[Pathname.MAX_PARTS];
	/**
   *
   */
	private int[] _endIndex = new int[Pathname.MAX_PARTS];
	/**
   *
   */
	protected Pathname _masterPath = null;
	/**
   *
   */
	static final int MAX_LINE_LENGTH = 132;
	/**
   *
   */
	private static final boolean DEBUG = false;
}
