/**
 *
 */
package de.mpicbg.jug.clearvolume.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import clearvolume.renderer.ControlJPanel;

import com.jogamp.newt.awt.NewtCanvasAWT;


/**
 * @author jug
 */
public class GenericClearVolumeGui< T extends RealType< T > & NativeType< T >> extends JPanel implements ActionListener {

	private Container ctnrClearVolume;
	private NewtCanvasAWT newtClearVolumeCanvas;
	private JPanel panelControls;
	private JButton buttonReinitializeView;
	private JButton buttonResetView;
	private JButton buttonUpdateView;
	private JTextField txtTextureWidth;
	private JTextField txtTextureHeight;
	private JTextField txtMinInt;
	private JTextField txtMaxInt;
	private JTextField txtVoxelSizeX;
	private JTextField txtVoxelSizeY;
	private JTextField txtVoxelSizeZ;
	private JButton buttonToggleBox;
	private JButton buttonToggleRecording;

	private int maxTextureWidth;
	private int maxTextureHeight;

	private ImgPlus< T > imgPlus;
	private List< RandomAccessibleInterval< T >> images;
	private ClearVolumeManager< T > cvManager;

	public GenericClearVolumeGui( final ImgPlus< T > imgPlus ) {
		this( imgPlus, 512, 512 );
	}

	public GenericClearVolumeGui( final ImgPlus< T > imgPlus, final int textureWidth, final int textureHeight ) {
		super( true );

		this.imgPlus = imgPlus;
		images = new ArrayList< RandomAccessibleInterval< T >>();
		setTextureSize( textureWidth, textureHeight );

		if ( imgPlus != null ) {
			setImagesFromImgPlus( imgPlus );
			launchClearVolumeManager();
		}
	}

	public void setImagesFromImgPlus( final ImgPlus< T > imgPlus ) {
		if ( imgPlus == null ) return;

		// if given imgPlus has multiple channels: separate them!
		if ( imgPlus.numDimensions() == 3 ) {
			images.add( imgPlus );
		} else if ( imgPlus.numDimensions() == 4 ) {
			for ( int channel = 0; channel < imgPlus.dimension( 2 ); channel++ ) {
				final RandomAccessibleInterval< T > rai = Views.hyperSlice( imgPlus, 2, channel );
				images.add( rai );
			}
		}
	}

	public void launchClearVolumeManager() {
		// if cvManager is set from previous session - free everything!
		if ( cvManager != null ) {
			cvManager.close();
			this.closeOldSession();
		}

		// instantiate a NEW ClearVolumeManager
		cvManager = new ClearVolumeManager< T >( images, maxTextureWidth, maxTextureHeight );
		if ( imgPlus.numDimensions() == 3 ) {
			cvManager.setVoxelSize(
					imgPlus.averageScale( 0 ),
					imgPlus.averageScale( 1 ),
					imgPlus.averageScale( 2 ) );
		} else if ( imgPlus.numDimensions() == 4 ) {
			cvManager.setVoxelSize(
					imgPlus.averageScale( 0 ),
					imgPlus.averageScale( 1 ),
					imgPlus.averageScale( 3 ) );
		}
		cvManager.run();
		buildGui();
	}

	public void relaunchClearVolumeManager( final ClearVolumeManager oldManager ) {
		final double[] oldMinI = oldManager.getMinIntensities();
		final double[] oldMaxI = oldManager.getMaxIntensities();
		final List< RandomAccessibleInterval< T >> oldImages = oldManager.getChannelImages();
		final double oldVoxelSizeX = oldManager.getVoxelSizeX();
		final double oldVoxelSizeY = oldManager.getVoxelSizeY();
		final double oldVoxelSizeZ = oldManager.getVoxelSizeZ();

		oldManager.close();
		this.closeOldSession();

		// instantiate a NEW ClearVolumeManager using the old images and params
		cvManager = new ClearVolumeManager< T >( oldImages, maxTextureWidth, maxTextureHeight );
		cvManager.setVoxelSize( oldVoxelSizeX, oldVoxelSizeY, oldVoxelSizeZ );
		for ( int i = 0; i < oldImages.size(); i++ ) {
			cvManager.setIntensityValues( i, oldMinI[ i ], oldMaxI[ i ] );
		}

		cvManager.run();
		buildGui();
	}

	private void setTextureSize( final int textureWidth, final int textureHeight ) {
		this.maxTextureWidth = textureWidth;
		this.maxTextureHeight = textureHeight;
		if ( cvManager != null ) {
			cvManager.setTextureSize( textureWidth, textureHeight );
		}
	}

	public ClearVolumeManager< T > getClearVolumeManager() {
		return cvManager;
	}

	public void pushParamsToGui() {
		txtTextureWidth.setText( "" + cvManager.getTextureWidth() );
		txtTextureHeight.setText( "" + cvManager.getTextureHeight() );

		txtMinInt.setText( "" + cvManager.getMinIntensity( cvManager.getActiveChannelIndex() ) );
		txtMaxInt.setText( "" + cvManager.getMaxIntensity( cvManager.getActiveChannelIndex() ) );

		txtVoxelSizeX.setText( "" + cvManager.getVoxelSizeX() );
		txtVoxelSizeY.setText( "" + cvManager.getVoxelSizeY() );
		txtVoxelSizeZ.setText( "" + cvManager.getVoxelSizeZ() );
	}

	/**
	 * Read all validly entered text field values and activate them.
	 */
	private void activateGuiValues() {
		int i;
		double d;

		try {
			i = Integer.parseInt( txtTextureWidth.getText() );
		} catch ( final NumberFormatException e ) {
			i = this.maxTextureWidth;
		}
		this.maxTextureWidth = i;

		try {
			i = Integer.parseInt( txtTextureHeight.getText() );
		} catch ( final NumberFormatException e ) {
			i = this.maxTextureHeight;
		}
		this.maxTextureHeight = i;

		cvManager.setTextureSize( this.maxTextureWidth, this.maxTextureHeight );

		try {
			d = Double.parseDouble( txtMinInt.getText() );
		} catch ( final NumberFormatException e ) {
			d = cvManager.getMinIntensity(cvManager.getActiveChannelIndex());
		}
		final double minIntensity = d;

		try {
			d = Double.parseDouble( txtMaxInt.getText() );
		} catch ( final NumberFormatException e ) {
			d = cvManager.getMaxIntensity(cvManager.getActiveChannelIndex());
		}
		final double maxIntensity = d;

		cvManager.setIntensityValues( cvManager.getActiveChannelIndex(), minIntensity, maxIntensity );

		try {
			d = Double.parseDouble( txtVoxelSizeX.getText() );
		} catch ( final NumberFormatException e ) {
			d = cvManager.getVoxelSizeX();
		}
		final double voxelSizeX = d;

		try {
			d = Double.parseDouble( txtVoxelSizeY.getText() );
		} catch ( final NumberFormatException e ) {
			d = cvManager.getVoxelSizeY();
		}
		final double voxelSizeY = d;

		try {
			d = Double.parseDouble( txtVoxelSizeZ.getText() );
		} catch ( final NumberFormatException e ) {
			d = cvManager.getVoxelSizeZ();
		}
		final double voxelSizeZ = d;

		cvManager.setVoxelSize( voxelSizeX, voxelSizeY, voxelSizeZ );
	}

	private void buildGui() {
//		this.setIgnoreRepaint( true );
		this.setVisible( false );
		this.removeAll();

		this.setLayout( new BorderLayout() );

		ctnrClearVolume = new Container();
		ctnrClearVolume.setLayout( new BorderLayout() );

		ControlJPanel panelClearVolumeControl = null;

		if ( cvManager != null ) {
			newtClearVolumeCanvas = cvManager.getClearVolumeRendererInterface().getNewtCanvasAWT();
			ctnrClearVolume.add( newtClearVolumeCanvas, BorderLayout.CENTER );

			panelClearVolumeControl = new ControlJPanel();
			panelClearVolumeControl.setClearVolumeRendererInterface( cvManager.getClearVolumeRendererInterface() );
		} else {
			System.err.println( "ClearVolumeTableCellView: Did you intend this? You called buildGui while cvManager==null!" );
		}

		// Main controls panel
		// -------------------
		panelControls = new JPanel();
		panelControls.setLayout( new BoxLayout( panelControls, BoxLayout.Y_AXIS ) );
		panelControls.add( Box.createVerticalGlue() );


		// Parameters requiring reinitialization
		// -------------------------------------
		JPanel panelControlsHelper = new JPanel( new GridLayout( 4, 2 ) );
		panelControlsHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );

		final JLabel lblMinInt = new JLabel( "Min. intensity" );
		txtMinInt = new JTextField();
		txtMinInt.setActionCommand( "Restart" );
		txtMinInt.addActionListener( this );
		final JLabel lblMaxInt = new JLabel( "Max. intensity" );
		txtMaxInt = new JTextField();
		txtMaxInt.setActionCommand( "Restart" );
		txtMaxInt.addActionListener( this );

		final JLabel lblTextureWidth = new JLabel( "Texture width" );
		txtTextureWidth = new JTextField();
		txtTextureWidth.setActionCommand( "Restart" );
		txtTextureWidth.addActionListener( this );
		final JLabel lblTextureHeight = new JLabel( "Texture height" );
		txtTextureHeight = new JTextField();
		txtTextureHeight.setActionCommand( "Restart" );
		txtTextureHeight.addActionListener( this );

		panelControlsHelper.add( lblMinInt );
		panelControlsHelper.add( txtMinInt );
		panelControlsHelper.add( lblMaxInt );
		panelControlsHelper.add( txtMaxInt );

		panelControlsHelper.add( lblTextureWidth );
		panelControlsHelper.add( txtTextureWidth );
		panelControlsHelper.add( lblTextureHeight );
		panelControlsHelper.add( txtTextureHeight );

		JPanel shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( panelControlsHelper, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );
		panelControls.add( shrinkingHelper );

		buttonReinitializeView = new JButton( "Set" );
		buttonReinitializeView.addActionListener( this );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( buttonReinitializeView, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 22, 2 ) );
		panelControls.add( shrinkingHelper );

		// Parameters that require a view update
		// -------------------------------------
		panelControlsHelper = new JPanel( new GridLayout( 3, 2 ) );
		panelControlsHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );

		final JLabel lblVoxelSizeX = new JLabel( "VoxelDimension.X" );
		txtVoxelSizeX = new JTextField();
		txtVoxelSizeX.setActionCommand( "UpdateView" );
		txtVoxelSizeX.addActionListener( this );
		final JLabel lblVoxelSizeY = new JLabel( "VoxelDimension.Y" );
		txtVoxelSizeY = new JTextField();
		txtVoxelSizeY.setActionCommand( "UpdateView" );
		txtVoxelSizeY.addActionListener( this );
		final JLabel lblVoxelSizeZ = new JLabel( "VoxelDimension.Z" );
		txtVoxelSizeZ = new JTextField();
		txtVoxelSizeZ.setActionCommand( "UpdateView" );
		txtVoxelSizeZ.addActionListener( this );

		panelControlsHelper.add( lblVoxelSizeX );
		panelControlsHelper.add( txtVoxelSizeX );
		panelControlsHelper.add( lblVoxelSizeY );
		panelControlsHelper.add( txtVoxelSizeY );
		panelControlsHelper.add( lblVoxelSizeZ );
		panelControlsHelper.add( txtVoxelSizeZ );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( panelControlsHelper, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );
		panelControls.add( shrinkingHelper );

		buttonUpdateView = new JButton( "Set" );
		buttonUpdateView.addActionListener( this );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( buttonUpdateView, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );
		panelControls.add( shrinkingHelper );

		buttonResetView = new JButton( "Reset" );
		buttonResetView.addActionListener( this );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( buttonResetView, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 22, 2 ) );
		panelControls.add( shrinkingHelper );

		// Toggle-buttons
		// --------------
		buttonToggleBox = new JButton( "Show/Unshow Box" );
		buttonToggleBox.addActionListener( this );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( buttonToggleBox, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );
		panelControls.add( shrinkingHelper );

		buttonToggleRecording = new JButton( "Start/Stop Recording" );
		buttonToggleRecording.addActionListener( this );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( buttonToggleRecording, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 22, 2 ) );
		panelControls.add( shrinkingHelper );

		// Display hijacked control container if possible
		// ----------------------------------------------
		if ( panelClearVolumeControl != null ) {
			this.add( panelClearVolumeControl, BorderLayout.SOUTH );
		}

		this.add( ctnrClearVolume, BorderLayout.CENTER );

		final JPanel helperPanel = new JPanel( new BorderLayout() );
		helperPanel.add( panelControls, BorderLayout.NORTH );

		final JScrollPane scrollPane = new JScrollPane( helperPanel );
		scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		this.add( scrollPane, BorderLayout.EAST );

		// Update the values in the gui fields
		pushParamsToGui();

//		this.setIgnoreRepaint( false );
		this.setVisible( true );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed( final ActionEvent e ) {

		if ( e.getSource().equals( buttonReinitializeView ) || e.getActionCommand().equals( "Restart" ) ) {
			activateGuiValues();
			relaunchClearVolumeManager( cvManager );
		} else if ( e.getSource().equals( buttonUpdateView ) || e.getActionCommand().equals( "UpdateView" ) ) {
			activateGuiValues();
			cvManager.updateView();
		} else if ( e.getSource().equals( buttonResetView ) ) {
			cvManager.setVoxelSize( imgPlus.averageScale( 0 ), imgPlus.averageScale( 1 ), imgPlus.averageScale( 2 ) );
			pushParamsToGui();
			cvManager.resetView();
		} else if ( e.getSource().equals( buttonToggleBox ) ) {
			cvManager.toggleBox();
		} else if ( e.getSource().equals( buttonToggleRecording ) ) {
			cvManager.toggleRecording();
		}

	}

	/**
	 * Cleans up all ClearVolume resources and empties this panel.
	 */
	public void closeOldSession() {
		if ( newtClearVolumeCanvas != null ) ctnrClearVolume.remove( newtClearVolumeCanvas );
		if ( cvManager != null ) cvManager.close();
		this.removeAll();
	}

}
