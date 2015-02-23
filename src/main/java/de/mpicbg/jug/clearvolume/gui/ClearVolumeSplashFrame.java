/**
 *
 */
package de.mpicbg.jug.clearvolume.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import clearvolume.network.client.main.ClearVolumeClientMain;
import clearvolume.utils.AppleMac;

/**
 * @author jug
 */
public class ClearVolumeSplashFrame extends JFrame {

	private final int windowWidth = 450;
	private final int windowHeight = 160;

	Thread progressThread;

	public ClearVolumeSplashFrame() {
		super( "Loading ClearVolume..." );

		final Dimension screenDims = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

		this.setLayout( new BorderLayout() );
		this.setBounds(
				( screenDims.width - windowWidth ) / 2,
				( screenDims.height - windowHeight ) / 2,
				windowWidth,
				windowHeight );

		this.setResizable( false );
		this.getContentPane().setBackground( Color.WHITE );

		final JLabel label = new JLabel( "" );
		label.setBounds( 8, 5, 512, 157 );
		label.setHorizontalTextPosition( SwingConstants.CENTER );
		label.setHorizontalAlignment( SwingConstants.CENTER );
		label.setIcon( new ImageIcon( ClearVolumeClientMain.class.getResource( "images/ClearVolumeLogo_cropped.png" ) ) );
		this.add( label, BorderLayout.CENTER );

		final JProgressBar bar = new JProgressBar( JProgressBar.HORIZONTAL, 0, 100 );
		this.add( bar, BorderLayout.SOUTH );

		progressThread = new Thread( new Runnable() {

			@Override
			public void run() {
				try {
					while ( true ) {
						SwingUtilities.invokeLater( new Runnable() {

							@Override
							public void run() {
								int v = bar.getValue() + 1;
								if ( v > 100 ) {
									v = 0;
								}
								bar.setValue( v );
								bar.repaint();
							}
						} );
						Thread.sleep( 100 );
					}
				} catch ( final Throwable t ) {
					t.printStackTrace();
				}
			}

		} );
		progressThread.setDaemon( true );
		progressThread.start();

		final ClearVolumeSplashFrame self = this;
		if ( javax.swing.SwingUtilities.isEventDispatchThread() ) {
			self.setClearVolumeIcon( self );
			self.setVisible( true );
		} else {
			SwingUtilities.invokeLater( new Runnable() {

				@Override
				public void run() {
					self.setClearVolumeIcon( self );
					self.setVisible( true );
				}

			} );
		}
	}

	@Override
	public void dispose() {
		progressThread.stop();

		final ClearVolumeSplashFrame self = this;
		SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {
				self.setVisible( false );
				ClearVolumeSplashFrame.super.dispose();
			}
		} );
	}

	private void setClearVolumeIcon( final JFrame frame ) {
		try
		{
			final URL lImageURL =
					getClass().getResource( "/clearvolume/icon/ClearVolumeIcon256.png" );
			final ImageIcon lImageIcon = new ImageIcon( lImageURL );

			if ( AppleMac.isMac() )
			{
				AppleMac.setApplicationIcon( lImageIcon.getImage() );
				AppleMac.setApplicationName( "ClearVolume" );
			}

			frame.setIconImage( lImageIcon.getImage() );
		} catch ( final Throwable e ) {
			e.printStackTrace();
		}

	}
}
