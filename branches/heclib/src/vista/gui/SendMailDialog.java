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
package vista.gui;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A simple gui to sending mail.
 * 
 * @author Nicky Sandhu
 * @version $Id: SendMailDialog.java,v 1.1 2003/10/02 20:49:15 redwood Exp $
 */
public class SendMailDialog extends JDialog implements Changeable {
	private JTextField _toTF, _subjectTF;
	private JTextArea _msgTA;

	/**
	 * constructs a send mail dialog with recipients, subject and initial
	 * message
	 * 
	 * @param recipients
	 *            A "," delimited string of recipients
	 * @param subject
	 *            A string containing the subject
	 * @param message
	 *            A message with lines delimited by "\n"
	 */
	public SendMailDialog(Frame parent, String recipients, String subject,
			String message) {
		super(parent);
		setTitle("Send Mail To: ");
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getMsgPanel(recipients, subject, message),
				BorderLayout.CENTER);
		getContentPane().add(new DialogButtonPanel(this), BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}

	/**
   *
   */
	private JPanel getMsgPanel(String recipients, String subject, String message) {
		// recipient
		JPanel toPanel = new JPanel();
		toPanel.setLayout(new BorderLayout());
		toPanel.add(new JLabel("To: "), BorderLayout.WEST);
		toPanel
				.add(_toTF = new JTextField(recipients, 20),
						BorderLayout.CENTER);
		// subject
		JPanel subjectPanel = new JPanel();
		subjectPanel.setLayout(new BorderLayout());
		subjectPanel.add(new JLabel("Subject: "), BorderLayout.WEST);
		subjectPanel.add(_subjectTF = new JTextField(subject, 20),
				BorderLayout.CENTER);
		// add to and subject to panel
		JPanel tosPanel = new JPanel();
		tosPanel.setLayout(new BorderLayout());
		tosPanel.add(toPanel, BorderLayout.NORTH);
		tosPanel.add(subjectPanel, BorderLayout.SOUTH);
		// body
		_msgTA = new JTextArea(message, 40, 20);
		_msgTA.setEditable(true);
		// add to, subject and body to main panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(tosPanel, BorderLayout.NORTH);
		mainPanel.add(_msgTA, BorderLayout.CENTER);
		return mainPanel;
	}

	/**
	 * sends mail given the current state of this dialog
	 */
	public void sendMail() {
		String to = _toTF.getText();
		String subject = _subjectTF.getText();
		String message = _msgTA.getText();
		// Get a Session object
		Session session = Session.getDefaultInstance(System.getProperties(),
				null);
		// construct the message
		Message msg = new MimeMessage(session);
		try {
			msg.setFrom();
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(
					to, false));
			msg.setSubject(subject);
			msg.setText(message);
			msg.setHeader("X-Mailer", "Vista");
			// send the thing off
			Transport.send(msg);
		} catch (Exception me) {
			System.out.println("Could not send message: " + me.getMessage());
		}

	}

	/**
   *
   */
	public void applyChanges() {
		this.sendMail();
	}

	/**
   *
   */
	public void doneChanges() {
		this.setVisible(false);
		this.dispose();
	}
}
