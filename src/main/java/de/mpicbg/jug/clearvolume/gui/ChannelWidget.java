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
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
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

	private final JButton bActivate;
	private final JButton bVisible;
	private final JSlider sBrightness;
	private final JButton bTransferFunction;

	public ChannelWidget( final ClearVolumeManager< ? > model, final int channelId ) {
		this.cvm = model;
		this.channelId = channelId;

		bActivate = new JButton();
		bActivate.addActionListener( this );
		setChannelActivationButtonIcon();

		bVisible = new JButton();
		bVisible.addActionListener( this );
		setVisibleIcon();

		sBrightness =
				new JSlider( JSlider.HORIZONTAL, 0, 100, ( int ) ( cvm.getBrightness( channelId ) * 100 ) );
		sBrightness.addChangeListener( this );
		sBrightness.addFocusListener( this );
		bTransferFunction = new JButton( model.getTransferFunctionColorIcon( channelId ) );
		bTransferFunction.addActionListener( this );

//		final JPanel panelHelper = new JPanel();
//		panelHelper.setLayout( new BoxLayout( panelHelper, BoxLayout.X_AXIS ) );
//		panelHelper.add( bActivate );
//		panelHelper.add( bVisible );

		this.setLayout( new BorderLayout() );
//		this.add( new JLabel( String.format( "Ch.%d", channelId ) ), BorderLayout.NORTH );
		this.add( bActivate, BorderLayout.NORTH );
		this.add( bVisible, BorderLayout.WEST );
		this.add( sBrightness, BorderLayout.CENTER );
		this.add( bTransferFunction, BorderLayout.EAST );
	}

	/**
	 */
	private void setChannelActivationButtonIcon() {
		bActivate.setText( "Channel " + channelId );
		bActivate.setForeground( Color.gray );
	}

	/**
	 */
	private void setVisibleIcon() {
		try {
			URL iconURL = ClassLoader.getSystemResource( "isVisibleTrue.gif" );
			if ( iconURL == null ) {
				iconURL = getClass().getClassLoader().getResource( "isVisibleTrue.gif" );
			}
			final Image img = ImageIO.read( iconURL );
			bVisible.setIcon( new ImageIcon( img.getScaledInstance(
					20,
					20,
					java.awt.Image.SCALE_SMOOTH ) ) );
		} catch ( final Exception e ) {
			e.printStackTrace();
			bVisible.setText( "√" );
			bVisible.setForeground( Color.green );
		}
	}

	/**
	 */
	private void setInvisibleIcon() {
		try {
			URL iconURL = ClassLoader.getSystemResource( "isVisibleFalse.gif" );
			if ( iconURL == null ) {
				iconURL = getClass().getClassLoader().getResource( "isVisibleFalse.gif" );
			}
			final Image img = ImageIO.read( iconURL );
			bVisible.setIcon( new ImageIcon( img.getScaledInstance(
					20,
					20,
					java.awt.Image.SCALE_SMOOTH ) ) );
		} catch ( final Exception e ) {
			e.printStackTrace();
			bVisible.setText( "X" );
			bVisible.setForeground( Color.red );
		}
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
				setVisibleIcon();
			} else {
				visible = false;
				setInvisibleIcon();
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
				cvm.setTransferFunction( channelId, tf );
				bTransferFunction.setIcon( new TransferFunctionGradientIcon( 20, 20, tf ) );
			}

			cvm.setActiveChannelIndex( channelId );
			cvm.updateView();
		} else if ( e.getSource().equals( bActivate ) ) {
			cvm.setActiveChannelIndex( channelId );
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
