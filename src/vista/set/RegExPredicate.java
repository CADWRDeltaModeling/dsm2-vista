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

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import COM.objectspace.jgl.UnaryPredicate;

/**
 * Uses a regular expression to determine filtering.
 */
public abstract class RegExPredicate implements UnaryPredicate {
	/**
	 * expression type for using perl 5 regular expressions
	 */
	public static final int PERL5 = 1;
	/**
	 * expression type for using awk regular expressions
	 */
	public static final int AWK = 2;

	/**
	 * initializes the regular expression compilers
	 */
	public RegExPredicate(String regex) {
		_expType = PERL5; // awk is giving trouble in jdk1.2
		setRegularExpression(regex);
	}

	/**
	 * implements the method to determine the filtering criteria.
	 */
	public abstract boolean execute(Object first);

	/**
	 * true if regular expression grammar is perl
	 */
	public boolean isPerl() {
		return (_expType == PERL5);
	}

	/**
	 * true if regular expression grammar is perl
	 */
	public boolean isAwk() {
		return (_expType == AWK);
	}

	/**
	 * sets the regular expression. This may throw a runtime exception if the
	 * grammar and regular expression do not match.
	 */
	public final void setRegularExpression(String regex) {
		_regularExp = regex.toUpperCase();
		initRegEx(_regularExp);
	}

	/**
	 * returns the regular expression in use.
	 */
	public final String getRegularExpression() {
		return _regularExp;
	}

	/**
	 * sets type of regular expression grammar
	 */
	public final void setExpressionType(int type) {
		_expType = type;
		setRegularExpression(_regularExp);
	}

	/**
	 * initializes the regular expression by compiling it into the grammar
	 */
	private void initRegEx(String regex) {
		if (isPerl())
			createPerl5RegEx();
		else if (isAwk())
			createAwkRegEx();
		try {
			_pattern = _compiler.compile(regex);
		} catch (MalformedPatternException mpe) {
			mpe.printStackTrace();
			throw new RuntimeException("Incorrect Regular Expression " + regex);
		}
	}

	/**
	 * create perl 5 grammar
	 */
	private void createPerl5RegEx() {
		_compiler = new Perl5Compiler();
		_matcher = new Perl5Matcher();
	}

	/**
	 * create awk grammar
	 */
	private void createAwkRegEx() {
		// _compiler = new AwkCompiler();
		// _matcher = new AwkMatcher();
	}

	/**
	 * The pattern to be matched
	 */
	protected Pattern _pattern;
	/**
	 * The pattern match maker
	 */
	protected PatternMatcher _matcher;
	/**
	 * regular expression to be matched
	 */
	private String _regularExp;
	/**
	 * Type of expression, currently either PERL5 or AWK
	 */
	private int _expType;
	/**
   * 
   */
	private PatternCompiler _compiler;
}
