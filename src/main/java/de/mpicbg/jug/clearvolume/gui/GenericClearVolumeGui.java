/**
 *
 */
package de.mpicbg.jug.clearvolume.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
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
import javax.swing.SwingUtilities;

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
public class GenericClearVolumeGui< T extends RealType< T > & NativeType< T >> extends JPanel
		implements
		ActionListener,
		ActiveLayerListener {

	private Container ctnrClearVolume;
	private NewtCanvasAWT newtClearVolumeCanvas;
	private JPanel panelControls;

	private JButton buttonResetView;
	private JButton buttonUpdateView;

	private JTextField txtVoxelSizeX;
	private JTextField txtVoxelSizeY;
	private JTextField txtVoxelSizeZ;

	private JButton buttonToggleBox;
	private JButton buttonToggleRecording;
	private List< ChannelWidget > channelWidgets;
	ControlJPanel panelClearVolumeControl;

	private int maxTextureResolution;
	private boolean useCuda;

	private ImgPlus< T > imgPlus;
	private List< RandomAccessibleInterval< T >> images;
	private ClearVolumeManager< T > cvManager;

	public GenericClearVolumeGui( final ImgPlus< T > imgPlus ) {
		this( imgPlus, 768, true );
	}

	public GenericClearVolumeGui(
			final ImgPlus< T > imgPlus,
			final int textureResolution,
			final boolean useCuda ) {
		super( true );

		this.imgPlus = imgPlus;
		images = new ArrayList< RandomAccessibleInterval< T >>();
		setTextureSizeAndCudaFlag( textureResolution, useCuda );

		if ( imgPlus != null ) {
			setImagesFromImgPlus( imgPlus );
			launchClearVolumeManager();
		}
	}

	private void setImagesFromImgPlus( final ImgPlus< T > imgPlus ) {
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
		try {
			final GenericClearVolumeGui< T > self = this;
			final Runnable todo = new Runnable() {

				@Override
				public void run() {
					cvManager =
							new ClearVolumeManager< T >( images, maxTextureResolution, maxTextureResolution, useCuda );
					cvManager.addActiveLayerChangedListener( self );
				}
			};

			if ( javax.swing.SwingUtilities.isEventDispatchThread() ) {
				todo.run();
			} else {
				SwingUtilities.invokeAndWait( todo );
			}
		} catch ( InvocationTargetException | InterruptedException e ) {
			System.err.println( "Launching CV session was interrupted in GenericClearVolumeGui!" );
			e.printStackTrace();
		}

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

		// Create necessary channel widgets!
		this.channelWidgets = new ArrayList< ChannelWidget >();
		for ( int i = 0; i < images.size(); i++ ) {
			channelWidgets.add( new ChannelWidget( cvManager, i ) );
		}

		buildGui();
	}

	public void relaunchClearVolumeManager( final ClearVolumeManager< T > oldManager ) {
		final double[] oldMinI = oldManager.getMinIntensities();
		final double[] oldMaxI = oldManager.getMaxIntensities();
		final List< RandomAccessibleInterval< T >> oldImages = oldManager.getChannelImages();
		final double oldVoxelSizeX = oldManager.getVoxelSizeX();
		final double oldVoxelSizeY = oldManager.getVoxelSizeY();
		final double oldVoxelSizeZ = oldManager.getVoxelSizeZ();

		oldManager.close();
		this.closeOldSession();

		// instantiate a NEW ClearVolumeManager using the old images and params
		try {
			final GenericClearVolumeGui< T > self = this;
			final Runnable todo = new Runnable() {

				@Override
				public void run() {
					cvManager =
							new ClearVolumeManager< T >( oldImages, maxTextureResolution, maxTextureResolution, useCuda );
					cvManager.addActiveLayerChangedListener( self );
				}
			};

			if ( javax.swing.SwingUtilities.isEventDispatchThread() ) {
				todo.run();
			} else {
				SwingUtilities.invokeAndWait( todo );
			}
		} catch ( InvocationTargetException | InterruptedException e ) {
			System.err.println( "Relaunching CV session was interrupted in GenericClearVolumeGui!" );
		}

		cvManager.setVoxelSize( oldVoxelSizeX, oldVoxelSizeY, oldVoxelSizeZ );
		for ( int i = 0; i < oldImages.size(); i++ ) {
			cvManager.setIntensityValues( i, oldMinI[ i ], oldMaxI[ i ] );
		}

		cvManager.run();
		buildGui();
	}

	private void setTextureSizeAndCudaFlag( final int textureRes, final boolean useCuda ) {
		this.maxTextureResolution = textureRes;
		this.useCuda = useCuda;

		if ( cvManager != null ) {
			cvManager.setTextureSize( textureRes, textureRes );
			cvManager.setCuda( true );
		}
	}

	public ClearVolumeManager< T > getClearVolumeManager() {
		return cvManager;
	}

	public void pushParamsToGui() {
		txtVoxelSizeX.setText( "" + cvManager.getVoxelSizeX() );
		txtVoxelSizeY.setText( "" + cvManager.getVoxelSizeY() );
		txtVoxelSizeZ.setText( "" + cvManager.getVoxelSizeZ() );
	}

	/**
	 * Read all validly entered text field values and activate them.
	 */
	private void activateGuiValues() {
		final int i;
		double d;

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

		if ( cvManager != null ) {
			newtClearVolumeCanvas = cvManager.getClearVolumeRendererInterface().getNewtCanvasAWT();
			ctnrClearVolume.add( newtClearVolumeCanvas, BorderLayout.CENTER );

			panelClearVolumeControl =
					new ControlJPanel( cvManager.getActiveChannelIndex(), cvManager.getClearVolumeRendererInterface() );
			panelClearVolumeControl.setClearVolumeRendererInterface( cvManager.getClearVolumeRendererInterface() );
		} else {
			System.err.println( "ClearVolumeTableCellView: Did you intend this? You called buildGui while cvManager==null!" );
		}

		// Main controls panel
		// -------------------
		panelControls = new JPanel();
		panelControls.setLayout( new BoxLayout( panelControls, BoxLayout.Y_AXIS ) );
		panelControls.add( Box.createVerticalGlue() );


		// Parameters that require a view update
		// -------------------------------------
		JPanel panelControlsHelper = new JPanel( new GridLayout( 3, 2 ) );
		panelControlsHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );

		final JLabel lblVoxelSizeX = new JLabel( "VoxelDimension.X" );
		txtVoxelSizeX = new JTextField( 8 );
		txtVoxelSizeX.setActionCommand( "UpdateView" );
		txtVoxelSizeX.addActionListener( this );
		final JLabel lblVoxelSizeY = new JLabel( "VoxelDimension.Y" );
		txtVoxelSizeY = new JTextField( 8 );
		txtVoxelSizeY.setActionCommand( "UpdateView" );
		txtVoxelSizeY.addActionListener( this );
		final JLabel lblVoxelSizeZ = new JLabel( "VoxelDimension.Z" );
		txtVoxelSizeZ = new JTextField( 8 );
		txtVoxelSizeZ.setActionCommand( "UpdateView" );
		txtVoxelSizeZ.addActionListener( this );

		panelControlsHelper.add( lblVoxelSizeX );
		panelControlsHelper.add( txtVoxelSizeX );
		panelControlsHelper.add( lblVoxelSizeY );
		panelControlsHelper.add( txtVoxelSizeY );
		panelControlsHelper.add( lblVoxelSizeZ );
		panelControlsHelper.add( txtVoxelSizeZ );

		JPanel shrinkingHelper = new JPanel( new BorderLayout() );
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

		// Channel Widgets
		// ===============
		panelControlsHelper = new JPanel( new GridLayout( channelWidgets.size(), 1 ) );
		panelControlsHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );

		for ( int i = 0; i < channelWidgets.size(); i++ ) {
			panelControlsHelper.add( channelWidgets.get( i ) );
		}

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( panelControlsHelper, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );
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

		if ( e.getSource().equals( buttonUpdateView ) || e.getActionCommand().equals( "UpdateView" ) ) {
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
		try {
			final GenericClearVolumeGui< T > self = this;
			final Runnable todo = new Runnable() {

				@Override
				public void run() {
					if ( newtClearVolumeCanvas != null )
						ctnrClearVolume.remove( newtClearVolumeCanvas );
					if ( cvManager != null ) cvManager.close();
					self.removeAll();
				}
			};

			if ( javax.swing.SwingUtilities.isEventDispatchThread() ) {
				todo.run();
			} else {
				SwingUtilities.invokeAndWait( todo );
			}
		} catch ( InvocationTargetException | InterruptedException e ) {
			System.err.println( "Closing of an old CV session was interrupted in GenericClearVolumeGui!" );
		}
	}

	/**
	 * @see de.mpicbg.jug.clearvolume.gui.ActiveLayerListener#activeLayerChanged(int)
	 */
	@Override
	public void activeLayerChanged( final int layerId ) {
		this.remove( panelClearVolumeControl );
		panelClearVolumeControl =
				new ControlJPanel( cvManager.getActiveChannelIndex(), cvManager.getClearVolumeRendererInterface() );
		this.add( panelClearVolumeControl, BorderLayout.SOUTH );

		final GenericClearVolumeGui< T > self = this;
		SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {
				self.revalidate();
			}
		} );
	}

}
