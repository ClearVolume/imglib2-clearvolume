/**
 *
 */
package de.mpicbg.jug.clearvolume.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * @author jug
 */
public class CreditsDialog extends JDialog implements ActionListener {

	public CreditsDialog( final JPanel parent ) {
		super( SwingUtilities.getWindowAncestor( parent ) );

		final int w = 640;
		final int h = 520;
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final int screenX = ( int ) screenSize.getWidth();
		final int screenY = ( int ) screenSize.getHeight();
		this.setBounds( ( screenX - w ) / 2, ( screenY - h ) / 2, w, h );

		this.getContentPane().setLayout( new BorderLayout() );

		final Container cp = this.getContentPane();

		final JEditorPane editor = new JEditorPane();
		editor.setEditable( false );

		try {
			URL textURL = ClassLoader.getSystemResource( "creditsAndShortcuts.html" );
			if ( textURL == null ) {
				textURL = getClass().getClassLoader().getResource( "creditsAndShortcuts.html" );
			}
			editor.setPage( textURL );
		} catch ( final Exception e ) {
			editor.setContentType( "text/html" );
			editor.setText( "<H1>Ressource to display not found!</H1>" );
		}
		editor.putClientProperty( JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE );
		editor.setFont( new JButton().getFont() );

		cp.add( new JScrollPane( editor ), BorderLayout.CENTER );

		final JButton bClose = new JButton( "close" );
		bClose.addActionListener( this );
		cp.add( bClose, BorderLayout.SOUTH );

		setVisible( true );
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed( final ActionEvent e ) {
		setVisible( false );
		dispose();
	}
}
