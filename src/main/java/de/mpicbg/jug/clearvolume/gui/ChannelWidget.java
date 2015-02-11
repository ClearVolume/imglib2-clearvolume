/**
 *
 */
package de.mpicbg.jug.clearvolume.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * @author jug
 */
public class ChannelWidget extends JPanel implements ActionListener, ChangeListener {

	// The GUI widget's model (or something close to it)
	private final int channelId;
	private final ClearVolumeManager< ? > cvm;

	private final JButton bVisible;
	private final JSlider sBrightness;
	private final JButton bTransferFunction;

	public ChannelWidget( final ClearVolumeManager< ? > model, final int channelId ) {
		this.cvm = model;
		this.channelId = channelId;

		bVisible = new JButton();
		bVisible.addActionListener( this );
		try {
			final Image img =
					ImageIO.read( getClass().getResource( "resources/isVisibleTrue.bmp" ) );
			bVisible.setIcon( new ImageIcon( img ) );
		} catch ( final Exception e ) {
			bVisible.setText( "√" );
			bVisible.setForeground( Color.green );
		}
		sBrightness =
				new JSlider( JSlider.HORIZONTAL, 0, 100, ( int ) ( cvm.getBrightness( channelId ) * 100 ) );
		sBrightness.addChangeListener( this );
		bTransferFunction = new JButton();
		bTransferFunction.addActionListener( this );
		bTransferFunction.setBackground( Color.black );

		this.setLayout( new BorderLayout() );
		this.add( new JLabel( String.format( "Ch.%d", channelId ) ), BorderLayout.NORTH );
		this.add( bVisible, BorderLayout.WEST );
		this.add( sBrightness, BorderLayout.CENTER );
		this.add( bTransferFunction, BorderLayout.EAST );
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed( final ActionEvent e ) {

		if ( e.getSource().equals( bVisible ) ) {
			boolean visible = cvm.isChannelVisible( channelId );
			if ( !visible ) {
				visible = true;
				cvm.setActiveChannelIndex( channelId );
				try {
					final Image img =
							ImageIO.read( getClass().getResource( "resources/isVisibleTrue.bmp" ) );
				bVisible.setIcon( new ImageIcon( img ) );
				} catch ( final Exception ioe ) {
					bVisible.setText( "√" );
					bVisible.setForeground( Color.green );
				}
			} else {
				visible = false;
				try {
					final Image img =
							ImageIO.read( getClass().getResource( "resources/isVisibleFalse.bmp" ) );
					bVisible.setIcon( new ImageIcon( img ) );
				} catch ( final Exception ioe ) {
					bVisible.setText( "X" );
					bVisible.setForeground( Color.red );
				}
			}
			cvm.setChannelVisible( channelId, visible );
		} else if ( e.getSource().equals( bTransferFunction ) ) {

		}
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged( final ChangeEvent e ) {
		final double brightness = sBrightness.getValue() / 100.0;
		System.out.println( String.format(
				"Brightness of channel %02d: %.2f",
				channelId,
				brightness ) );
		cvm.setBrightness( channelId, brightness );
		if ( cvm.isChannelVisible( channelId ) ) {
			cvm.setActiveChannelIndex( channelId );
		}
	}
}
