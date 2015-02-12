/**
 *
 */
package de.mpicbg.jug.clearvolume.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import clearvolume.transferf.TransferFunction;
import clearvolume.transferf.TransferFunctions;

/**
 * @author jug
 */
public class ChannelWidget extends JPanel implements ActionListener, ChangeListener, FocusListener {

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
					ImageIO.read( ClassLoader.getSystemResource( "isVisibleTrue.gif" ) );
			bVisible.setIcon( new ImageIcon( img.getScaledInstance(
					20,
					20,
					java.awt.Image.SCALE_SMOOTH ) ) );

		} catch ( final Exception e ) {
			e.printStackTrace();
			bVisible.setText( "√" );
			bVisible.setForeground( Color.green );
		}
		sBrightness =
				new JSlider( JSlider.HORIZONTAL, 0, 100, ( int ) ( cvm.getBrightness( channelId ) * 100 ) );
		sBrightness.addChangeListener( this );
		sBrightness.addFocusListener( this );
		bTransferFunction = new JButton( model.getTransferFunctionColorIcon( channelId ) );
		bTransferFunction.addActionListener( this );

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
							ImageIO.read( ClassLoader.getSystemResource( "isVisibleTrue.gif" ) );
					bVisible.setIcon( new ImageIcon( img.getScaledInstance(
							20,
							20,
							java.awt.Image.SCALE_SMOOTH ) ) );
				} catch ( final Exception ioe ) {
					bVisible.setText( "√" );
					bVisible.setForeground( Color.green );
				}
			} else {
				visible = false;
				try {
					final Image img =
							ImageIO.read( ClassLoader.getSystemResource( "isVisibleFalse.gif" ) );
					bVisible.setIcon( new ImageIcon( img.getScaledInstance(
							20,
							20,
							java.awt.Image.SCALE_SMOOTH ) ) );
				} catch ( final Exception ioe ) {
					bVisible.setText( "X" );
					bVisible.setForeground( Color.red );
				}
			}
			cvm.setChannelVisible( channelId, visible );

		} else if ( e.getSource().equals( bTransferFunction ) ) {
			final TransferFunction tfold = cvm.getTransferFunction(channelId);
			final float[] ftoldarray = tfold.getArray();
			final Color oldColor = new Color(
					ftoldarray[ ftoldarray.length - 4 ],
					ftoldarray[ ftoldarray.length - 3 ],
					ftoldarray[ ftoldarray.length - 2 ] );

			final Color newColor =
					JColorChooser.showDialog(
							SwingUtilities.getRoot( this ),
							"Choose channel color...",
							oldColor );
			TransferFunction tf;
			if ( newColor != null ) {
				tf = TransferFunctions.getGradientForColor(
						newColor.getRed() / 255f,
						newColor.getGreen() / 255f,
						newColor.getBlue() / 255f,
						newColor.getAlpha() / 255f );
			} else {
				tf = TransferFunctions.getRainbowSolid();
			}
			cvm.setTransferFunction( channelId, tf );
			bTransferFunction.setIcon( new TransferFunctionGradientIcon( 20, 20, tf ) );

			cvm.setActiveChannelIndex( channelId );
			cvm.updateView();
		}
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged( final ChangeEvent e ) {
		final double brightness = sBrightness.getValue() / 100.0;
		cvm.setBrightness( channelId, brightness );
		if ( cvm.isChannelVisible( channelId ) ) {
			cvm.setActiveChannelIndex( channelId );
		}
	}

	/**
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	@Override
	public void focusGained( final FocusEvent e ) {
		cvm.setActiveChannelIndex( channelId );
	}

	/**
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	@Override
	public void focusLost( final FocusEvent e ) {}
}
