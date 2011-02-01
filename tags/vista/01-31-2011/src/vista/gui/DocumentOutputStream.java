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
package vista.gui;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * An OutputStream implementation that places it's output in a swing text model
 * (Document). The Document can be either a plain text or styled document
 * implementation. If styled, the attributes assigned to the output stream will
 * be used in the display of the output.
 * 
 * @author Timothy Prinzing
 * @version 1.1 02/05/99
 */
public class DocumentOutputStream extends OutputStream {

	/**
	 * Constructs an output stream that will output to the given document with
	 * the given set of character attributes.
	 * 
	 * @param doc
	 *            the document to write to.
	 * @param a
	 *            the character attributes to use for the written text.
	 */
	public DocumentOutputStream(Document doc, AttributeSet a) {
		this.doc = doc;
		this.a = a;
	}

	/**
	 * Constructs an output stream that will output to the given document with
	 * whatever the default attributes are.
	 * 
	 * @param doc
	 *            the document to write to.
	 */
	public DocumentOutputStream(Document doc) {
		this(doc, null);
	}

	/**
	 * Writes the specified byte to this output stream.
	 * <p>
	 * Subclasses of <code>OutputStream</code> must provide an implementation
	 * for this method.
	 * 
	 * @param b
	 *            the <code>byte</code>.
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @since JDK1.0
	 */
	public void write(int b) throws IOException {
		one[0] = (byte) b;
		write(one, 0, 1);
	}

	/**
	 * Writes <code>len</code> bytes from the specified byte array starting at
	 * offset <code>off</code> to this output stream.
	 * <p>
	 * The <code>write</code> method of <code>OutputStream</code> calls the
	 * write method of one argument on each of the bytes to be written out.
	 * Subclasses are encouraged to override this method and provide a more
	 * efficient implementation.
	 * 
	 * @param b
	 *            the data.
	 * @param off
	 *            the start offset in the data.
	 * @param len
	 *            the number of bytes to write.
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @since JDK1.0
	 */
	public void write(byte b[], int off, int len) throws IOException {
		try {
			doc.insertString(doc.getLength(), new String(b, off, len), a);
		} catch (BadLocationException ble) {
			throw new IOException(ble.getMessage());
		}
	}

	private byte[] one = new byte[1];
	private Document doc;
	private AttributeSet a;
}
