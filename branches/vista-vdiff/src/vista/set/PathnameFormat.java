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
package vista.set;

/**
 * A format class for templates in pathname ids
 * 
 * @author Nicky Sandhu
 * @version $Id: PathnameFormat.java,v 1.1 2003/10/02 20:49:29 redwood Exp $
 */
public class PathnameFormat {
	/**
	 * takes a template string and the associated reference and generates a new
	 * string with the template filled in. The template is of the form (string)*
	 * (([A|B|C|D|E|F])(string))*
	 */
	public static String format(String template, DataReference ref) {
		String str = new String(template);
		Pathname path = ref.getPathname();
		str = replace(str, "[A]", path.getPart(Pathname.A_PART));
		str = replace(str, "[B]", path.getPart(Pathname.B_PART));
		str = replace(str, "[C]", path.getPart(Pathname.C_PART));
		str = replace(str, "[D]", path.getPart(Pathname.D_PART));
		str = replace(str, "[E]", path.getPart(Pathname.E_PART));
		str = replace(str, "[F]", path.getPart(Pathname.F_PART));
		return str;
	}

	/**
	 * formats a template with the given data references. Order of these
	 * references matter in the template The template is of the following form
	 * (string)* ([A|B|C|D|E|F($#)] string)* where $# is the $ sign with a
	 * number from 1 to refs.length
	 */
	public static String format(String template, DataReference[] refs) {
		String str = new String(template);
		for (int i = 0; i < refs.length; i++) {
			Pathname path = refs[i].getPathname();
			str = replace(str, "[A$" + (i + 1) + "]", path
					.getPart(Pathname.A_PART));
			str = replace(str, "[B$" + (i + 1) + "]", path
					.getPart(Pathname.B_PART));
			str = replace(str, "[C$" + (i + 1) + "]", path
					.getPart(Pathname.C_PART));
			str = replace(str, "[D$" + (i + 1) + "]", path
					.getPart(Pathname.D_PART));
			str = replace(str, "[E$" + (i + 1) + "]", path
					.getPart(Pathname.E_PART));
			str = replace(str, "[F$" + (i + 1) + "]", path
					.getPart(Pathname.F_PART));
		}
		return str;
	}

	/**
	 * replaces string "toBe" with string "with" in string "orig"
	 */
	public static String replace(String orig, String toBe, String with) {
		String nstr = orig;
		int index = nstr.indexOf(toBe);
		while (index >= 0) {
			int nl = nstr.length();
			int tbl = toBe.length();
			nstr = new StringBuffer(nl - tbl + with.length()).append(
					nstr.substring(0, index)).append(with).append(
					nstr.substring(index + tbl, nl)).toString();
			index = nstr.indexOf(toBe, index);
		}
		return nstr;
	}
}
